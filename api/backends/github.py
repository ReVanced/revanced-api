import asyncio
import os
from operator import eq
from platform import release
from typing import Optional

import ujson
from aiohttp import ClientResponse
from sanic import SanicException
from toolz import filter
from toolz.dicttoolz import get_in, keyfilter

from api.backends.backend import Backend, Repository
from api.backends.entities import *
from api.utils.http_utils import http_get

name: str = "github"
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
        super().__init__(name, base_url)
        self.token: Optional[str] = os.getenv("GITHUB_TOKEN")
        self.headers: dict[str, str] = {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
        }
        self.repositories_rest_endpoint: str = f"{base_url}/repos"

    async def __assemble_release(self, release: dict) -> Release:
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

        assets = await asyncio.gather(
            *[__assemble_asset(asset) for asset in release["assets"]]
        )

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
        if response.status == 200:
            releases: list[Release] = []
            releases = await asyncio.gather(
                *[
                    self.__assemble_release(release)
                    for release in await response.json(loads=ujson.loads)
                ]
            )
            return releases
        else:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )

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
        if response.status == 200:
            return await self.__assemble_release(await response.json(loads=ujson.loads))

        else:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )

    async def get_latest_release(
        self,
        repository: GithubRepository,
    ) -> Release:
        """Get the latest release for a given repository.

        Args:
            owner (str): The username or organization name that owns the repository.
            repo (str): The name of the repository.

        Returns:
            Release: The latest release for the given repository.
        """
        latest_release_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases/latest"
        response: ClientResponse = await http_get(
            headers=self.headers, url=latest_release_endpoint
        )
        if response.status == 200:
            return await self.__assemble_release(await response.json(loads=ujson.loads))

        else:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )

    async def get_latest_pre_release(
        self,
        repository: GithubRepository,
    ) -> Release:
        """Get the latest pre-release for a given repository.

        Args:
            owner (str): The username or organization name that owns the repository.
            repo (str): The name of the repository.

        Returns:
            Release: The latest pre-release for the given repository.
        """
        list_releases_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases?per_page=10&page=1"
        response: ClientResponse = await http_get(
            headers=self.headers, url=list_releases_endpoint
        )
        if response.status == 200:
            latest_pre_release: dict = list(
                filter(
                    lambda release: release["prerelease"] is True,
                    await response.json(loads=ujson.loads),
                )
            )[0]
            return await self.__assemble_release(latest_pre_release)

        else:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )

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

        contributors: list[Contributor] = []
        response: ClientResponse = await http_get(
            headers=self.headers, url=contributors_endpoint
        )
        if response.status == 200:
            contributors = await asyncio.gather(
                *[
                    __assemble_contributor(contributor)
                    for contributor in await response.json(loads=ujson.loads)
                ]
            )
            return contributors
        else:
            raise SanicException(
                context=await response.json(loads=ujson.loads),
                status_code=response.status,
            )

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

        data: ClientResponse = await http_get(
            headers=self.headers,
            url=await __fetch_download_url(
                await self.get_release_by_tag_name(
                    repository=repository, tag_name=tag_name
                )
            ),
        )

        if data.status == 200:
            return ujson.loads(await data.read())
        else:
            raise SanicException(
                context=await data.json(loads=ujson.loads),
                status_code=data.status,
            )
