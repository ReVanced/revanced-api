import asyncio
import os
from textwrap import dedent
from typing import Optional

import ujson
from aiohttp import ClientResponse
from gql import Client
from gql import gql
from gql.transport.aiohttp import AIOHTTPTransport
from graphql import DocumentNode
from sanic import SanicException

from api.backends.backend import Backend
from api.backends.backend import Repository
from api.backends.entities import *
from api.utils.http_utils import http_get

name: str = "github"
base_url: str = "https://api.github.com"
graphql_root: str = "https://api.github.com/graphql"


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
        self.graphql: Client = Client(
            transport=AIOHTTPTransport(url=graphql_root, headers=self.headers),
            fetch_schema_from_transport=True,
        )

    async def __assemble_release(self, release: dict) -> Release:
        assets: list[Asset] = []
        metadata = Metadata(
            tag_name=release["tag_name"],
            name=release["name"],
            draft=release["draft"],
            prerelease=release["prerelease"],
            created_at=release["created_at"],
            published_at=release["published_at"],
        )
        for asset in release["assets"]:
            assets.append(
                Asset(
                    name=asset["name"],
                    content_type=asset["content_type"],
                    download_url=asset["browser_download_url"],
                )
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
        # query_base: str = """
        # query {
        #     repository(owner: "$owner", name: "$name") {
        #         releases(first: 30) {
        #             nodes {
        #                 tagName
        #                 name
        #                 isDraft
        #                 isPrerelease
        #                 publishedAt
        #                 releaseAssets(first: 10) {
        #                     nodes {
        #                         name
        #                         contentType
        #                         downloadUrl
        #                     }
        #                 }
        #             }
        #         }
        #     }
        # }
        # """.replace("$owner", repository.owner).replace("$name", repository.name)
        # query: DocumentNode = gql(dedent(query_base))

        # query_result = await self.graphql.execute_async(
        #     query, variable_values={"owner": repository.owner, "name": repository.name}
        # )

        # print(query_result)

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

    async def get_release(self, repository: GithubRepository, id: int) -> Release:
        """
        Retrieves a specific release for a given Github repository.

        Args:
            repository (GithubRepository): The Github repository for which to retrieve the release.
            id (int): The ID of the release to retrieve.

        Returns:
            Release: The Release object representing the retrieved release.
        """
        raise NotImplementedError

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
        raise NotImplementedError

    async def get_latest_release(
        self,
        repository: GithubRepository,
        development: bool = False,
    ) -> Release:
        """Get the latest release for a given repository.

        Args:
            owner (str): The username or organization name that owns the repository.
            repo (str): The name of the repository.

        Returns:
            Release: The latest release for the given repository.
        """
        list_releases_endpoint: str = f"{self.repositories_rest_endpoint}/{repository.owner}/{repository.name}/releases/latest"
        response: ClientResponse = await http_get(
            headers=self.headers, url=list_releases_endpoint
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
        raise NotImplementedError

    async def get_release_notes(
        self, repository: GithubRepository, tag_name: str
    ) -> Notes:
        """Get the release notes for a given release tag.

        Args:
            repository (GithubRepository): The repository that contains the release.
            tag_name (str): The name of the release tag.

        Returns:
            Notes: The release notes for the given release.
        """
        raise NotImplementedError

    async def get_contributors(self, repository: GithubRepository) -> list[Contributor]:
        """Get a list of contributors for a given repository.

        Args:
            repository (GithubRepository): The repository for which to retrieve contributors.

        Returns:
            list[Contributor]: A list of contributors for the given repository.
        """
        raise NotImplementedError

    async def get_patches(self, repository: GithubRepository, tag_name: str) -> dict:
        """Get a dictionary of patch URLs for a given repository.

        Args:
            repository (GithubRepository): The repository for which to retrieve patches.
            tag_name: The name of the release tag.

        Returns:
            dict: A dictionary of patch URLs for the given repository.
        """
        raise NotImplementedError
