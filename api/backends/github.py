import os

import ujson
from aiohttp import ClientResponse

from api.backends.backend import Backend, Repository
from api.backends.entities import *
from api.utils.http_utils import http_get

name: str = "github"
base_url: str = "https://api.github.com"


class GithubRepository(Repository):
    def __init__(self, owner: str, name: str):
        super().__init__(Github())
        self.owner = owner
        self.name = name


class Github(Backend):
    def __init__(self):
        super().__init__(name, base_url)
        self.token = os.getenv("GITHUB_TOKEN")
        self.headers: dict[str, str] = {
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
        }

    async def list_releases(
        self, repository: GithubRepository, per_page: int, page: int
    ) -> list[Release]:
        list_releases_endpoint: str = f"{self.base_url}/repos/{repository.owner}/{repository.name}/releases?per_page={per_page}&page={page}"
        releases: list[Release] = []
        response: ClientResponse = await http_get(
            headers=self.headers, url=list_releases_endpoint
        )
        for release in await response.json(loads=ujson.loads):
            assets: list[Asset] = []
            metadata = Metadata(
                tag_name=release["tag_name"],
                name=release["name"],
                body=release["body"],
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
            releases.append(Release(metadata=metadata, assets=assets))
        return releases

    async def get_release(self, repository: GithubRepository, id: int) -> Release:
        raise NotImplementedError

    async def get_release_by_tag_name(
        self, repository: GithubRepository, tag_name: str
    ) -> Release:
        raise NotImplementedError

    async def get_latest_release(
        self,
        owner: str,
        repo: str,
    ) -> Release:
        raise NotImplementedError

    async def get_latest_pre_release(
        self,
        owner: str,
        repo: str,
    ) -> Release:
        raise NotImplementedError

    async def get_release_notes(
        self, repository: GithubRepository, tag_name: str
    ) -> Notes:
        raise NotImplementedError

    async def get_contributors(self, repository: GithubRepository) -> list[Contributor]:
        raise NotImplementedError

    async def get_patches(self, repository: GithubRepository) -> dict:
        raise NotImplementedError
