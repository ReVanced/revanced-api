"""
This module provides endpoints for compatibility with the old API.

Routes:
    - GET /<repo:str>/releases: Retrieve a list of releases for a GitHub repository.
    - GET /<repo:str>/releases/latest: Retrieve the latest release for a GitHub repository.
    - GET /<repo:str>/releases/tag/<tag:str>: Retrieve a specific release for a GitHub repository by its tag name.
    - GET /<repo:str>/contributors: Retrieve a list of contributors for a GitHub repository.
    - GET /patches/<tag:str>: Retrieve a list of patches for a given release tag.

"""
import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.backends.github import Github, GithubRepository
from api.models.github import *
from api.models.compat import ToolsResponseModel, ContributorsResponseModel
from config import compat_repositories, owner

compat: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))

github_backend: Github = Github()


@compat.get("/tools")
@openapi.definition(
    summary="Get patching tools' latest version.", response=[ToolsResponseModel]
)
async def tools(request: Request) -> JSONResponse:
    """
    Retrieve a list of releases for a GitHub repository.

    **Args:**
        - repo (str): The name of the GitHub repository to retrieve releases for.

    **Query Parameters:**
        - per_page (int): The number of releases to retrieve per page.
        - page (int): The page number of the releases to retrieve.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of releases.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data: dict[str, list] = {
        "tools": await github_backend.compat_get_tools(
            repositories=[
                GithubRepository(owner=owner, name=repo)
                for repo in compat_repositories
                if repo
                not in ["revanced-api", "revanced-releases-api", "revanced-website"]
            ],
            dev=True if request.args.get("dev") else False,
        )
    }

    return json(data, status=200)


@compat.get("/contributors")
@openapi.definition(
    summary="Get organization-wise contributors.", response=[ContributorsResponseModel]
)
async def contributors(request: Request) -> JSONResponse:
    """
    Retrieve a list of releases for a GitHub repository.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of releases.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data: dict[str, list] = {
        "repositories": await github_backend.compat_get_contributors(
            repositories=[
                GithubRepository(owner=owner, name=repo) for repo in compat_repositories
            ]
        )
    }

    return json(data, status=200)
