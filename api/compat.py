"""
This module provides endpoints for compatibility with the old API.

Routes:
    - GET /<repo:str>/releases: Retrieve a list of releases for a Github repository.
    - GET /<repo:str>/releases/latest: Retrieve the latest release for a Github repository.
    - GET /<repo:str>/releases/tag/<tag:str>: Retrieve a specific release for a Github repository by its tag name.
    - GET /<repo:str>/contributors: Retrieve a list of contributors for a Github repository.
    - GET /patches/<tag:str>: Retrieve a list of patches for a given release tag.

"""


from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.backends.entities import Release
from api.backends.github import Github, GithubRepository
from api.models.github import *
from config import compat_repositories, owner
from toolz import filter

github: Blueprint = Blueprint("old")

github_backend: Github = Github()


@github.get("/tools")
@openapi.definition(summary="Get patching tools' latest version.", response=[])
async def tools(request: Request) -> JSONResponse:
    """
    Retrieve a list of releases for a Github repository.

    **Args:**
        - repo (str): The name of the Github repository to retrieve releases for.

    **Query Parameters:**
        - per_page (int): The number of releases to retrieve per page.
        - page (int): The page number of the releases to retrieve.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of releases.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data: dict[str, list] = {
        "tools": await github_backend.get_tools(
            repositories=[
                GithubRepository(owner=owner, name=repo)
                for repo in compat_repositories
                if repo not in ["revanced-releases-api", "revanced-website"]
            ],
            dev=True if request.args.get("dev") else False,
        )
    }

    return json(data, status=200)
