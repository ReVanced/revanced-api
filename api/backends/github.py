import asyncio
import os
from distutils.version import LooseVersion
from operator import eq
from typing import Optional

import ujson
from aiohttp import ClientResponse
from sanic import SanicException
from sanic.log import logger
from toolz import filter, map
from toolz.dicttoolz import get_in, keyfilter
from toolz.itertoolz import mapcat

from api.backends.backend import Backend, Repository
from api.backends.entities import *
from api.utils.http_utils import http_get

repo_name: str = "github"
base_url: str = "https://api.github.com"


class GithubRepository(Repository):
    """
    A repository class that represents a Github repository.

    Args:
        owner (str): The username of the owner of the Github repository.
        name (str): The name of the Github repository.
    """

    def __init__(self, owner: str, name: str):
        """
        Initializes a new instance of the GithubRepository class.

        Args:
            owner (str): The username of the owner of the Github repository.
            name (str): The name of the Github repository.
        """
        super().__init__(Github())
        self.owner = owner
        self.name = name


class Github(Backend):
    """
    A backend class that interacts with the Github API.

    Attributes:
        name (str): The name of the Github backend.
        base_url (str): The base URL of the Github API.
        token (str): The Github access token used for authentication.
        headers (dict[str, str]): The HTTP headers to be sent with each request to the Github API.
    """

    def __init__(self):
        """
        Initializes a new instance of the Github class.
        """
        super().__init__(repo_name, base_url)
        self.token: Optional[str] = os.getenv("GITHUB_TOKEN")
        self.headers: dict[str, str] = {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
        }
        self.repositories_rest_endpoint: str = f"{base_url}/repos"

    @staticmethod
    async def __assemble_release(release: dict) -> Release:
        async def __assemble_asset(asset: dict) -> Asset:
            asset_data: dict = keyfilter(
                lambda key: key in {"name", "content_type", "browser_download_url"},
                asset,
            )
            return Asset(**asset_data)

        filter_metadata = keyfilter(
            lambda key: key
            in {
                "tag_name",
                "name",
                "draft",
                "prerelease",
                "created_at",
                "published_at",
                "body",
            },
            release,
        )
        metadata = Metadata(**filter_metadata)
        assets = await asyncio.gather(*map(__assemble_asset, release["assets"]))
        return Release(metadata=metadata, assets=assets)

    async def list_releases(
        self, repository: GithubRepository, per_page: int = 30, page: int = 1
    ) -> list[Release]:
        """
        Returns a list of Release objects for a given Github repository.

        Args:
            repository (GithubRepository): The Github repository for which to retrieve the releases.
            per_page (int): The number of releases to return per page.
            page (int): The page number of the releases to return.

        Returns:
            list[Release]: A list of Release objects.
        """
        list_releases_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases?per_page={per_page}&page={page}"
        response: ClientResponse = await http_get(
            headers=self.headers, url=list_releases_endpoint
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        releases: list[Release] = await asyncio.gather(
            *map(
                lambda release: self.__assemble_release(release),
                await response.json(loads=ujson.loads),
            )
        )
        return releases

    async def get_release_by_tag_name(
        self, repository: GithubRepository, tag_name: str
    ) -> Release:
        """
        Retrieves a specific release for a given Github repository by its tag name.

        Args:
            repository (GithubRepository): The Github repository for which to retrieve the release.
            tag_name (str): The tag name of the release to retrieve.

        Returns:
            Release: The Release object representing the retrieved release.
        """
        release_by_tag_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases/tags/{tag_name}"
        response: ClientResponse = await http_get(
            headers=self.headers, url=release_by_tag_endpoint
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        return await self.__assemble_release(await response.json(loads=ujson.loads))

    async def get_latest_release(
        self,
        repository: GithubRepository,
    ) -> Release:
        """Get the latest release for a given repository.

        Args:
            repository (GithubRepository): The Github repository for which to retrieve the release.

        Returns:
            Release: The latest release for the given repository.
        """
        latest_release_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases/latest"
        response: ClientResponse = await http_get(
            headers=self.headers, url=latest_release_endpoint
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        return await self.__assemble_release(await response.json(loads=ujson.loads))

    async def get_latest_pre_release(
        self,
        repository: GithubRepository,
    ) -> Release:
        """Get the latest pre-release for a given repository.

        Args:
            repository (GithubRepository): The Github repository for which to retrieve the release.

        Returns:
            Release: The latest pre-release for the given repository.
        """
        list_releases_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases?per_page=10&page=1"
        response: ClientResponse = await http_get(
            headers=self.headers, url=list_releases_endpoint
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        latest_pre_release = next(
            filter(
                lambda release: release["prerelease"],
                await response.json(loads=ujson.loads),
            )
        )
        return await self.__assemble_release(latest_pre_release)

    async def get_contributors(self, repository: GithubRepository) -> list[Contributor]:
        """Get a list of contributors for a given repository.

        Args:
            repository (GithubRepository): The repository for which to retrieve contributors.

        Returns:
            list[Contributor]: A list of contributors for the given repository.
        """

        async def __assemble_contributor(contributor: dict) -> Contributor:
            filter_contributor = keyfilter(
                lambda key: key in {"login", "avatar_url", "html_url", "contributions"},
                contributor,
            )
            return Contributor(**filter_contributor)

        contributors_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/contributors"
        response: ClientResponse = await http_get(
            headers=self.headers, url=contributors_endpoint
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        contributors: list[Contributor] = await asyncio.gather(
            *map(__assemble_contributor, await response.json(loads=ujson.loads))
        )

        return contributors

    async def get_patches(
        self, repository: GithubRepository, tag_name: str
    ) -> list[dict]:
        """Get a dictionary of patch URLs for a given repository.

        Args:
            repository (GithubRepository): The repository for which to retrieve patches.
            tag_name: The name of the release tag.

        Returns:
            list[dict]: A JSON object containing the patches.
        """

        async def __fetch_download_url(release: Release) -> str:
            asset = get_in(["assets"], release)
            patch_asset = next(
                filter(lambda x: eq(get_in(["name"], x), "patches.json"), asset), None
            )
            return get_in(["browser_download_url"], patch_asset)

        response: ClientResponse = await http_get(
            headers=self.headers,
            url=await __fetch_download_url(
                await self.get_release_by_tag_name(
                    repository=repository, tag_name=tag_name
                )
            ),
        )
        if response.status != 200:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )
        return ujson.loads(await response.read())

    async def get_tools(self, repositories: list[GithubRepository], dev: bool) -> list:
        """Get the latest releases for a set of repositories (v1 compat).

        Args:
            repositories (set[GithubRepository]): The repositories for which to retrieve releases.
            dev: If we should get the latest pre-release instead.

        Returns:
            list[dict[str, str]]: A JSON object containing the releases.
        """

        def transform(data, repository):
            """Transforms a dictionary from the input list into a list of dictionaries with the desired structure.

            Args:
                data(dict): A dictionary from the input list.

            Returns:
                _[list]: A list of dictionaries with the desired structure.
            """

            def process_asset(asset):
                """Transforms an asset dictionary into a new dictionary with the desired structure.

                Args:
                    asset(dict): An asset dictionary.

                Returns:
                    _[dict]: A new dictionary with the desired structure.
                """
                return {
                    "repository": f"{repository.owner}/{repository.name}",
                    "version": data["metadata"]["tag_name"],
                    "timestamp": data["metadata"]["published_at"],
                    "name": asset["name"],
                    "browser_download_url": asset["browser_download_url"],
                    "content_type": asset["content_type"],
                }

            return map(process_asset, data["assets"])

        results = await asyncio.gather(
            *map(
                lambda release: self.get_latest_release(release),
                repositories,
            )
        )

        return list(mapcat(lambda pair: transform(*pair), zip(results, repositories)))
