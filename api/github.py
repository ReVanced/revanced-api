"""
This module provides endpoints for interacting with the GitHub API.

Routes:
    - GET /<repo:str>/releases: Retrieve a list of releases for a GitHub repository.
    - GET /<repo:str>/releases/latest: Retrieve the latest release for a GitHub repository.
    - GET /<repo:str>/releases/tag/<tag:str>: Retrieve a specific release for a GitHub repository by its tag name.
    - GET /<repo:str>/contributors: Retrieve a list of contributors for a GitHub repository.
    - GET /patches/<tag:str>: Retrieve a list of patches for a given release tag.

"""

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.backends.entities import Release, Contributor
from api.backends.github import Github, GithubRepository
from api.models.github import *
from config import owner, default_repository, api_version

github: Blueprint = Blueprint("github", version=api_version)

github_backend: Github = Github()


@github.get("/<repo:str>/releases")
@openapi.definition(
    summary="Get releases for a repository", response=[ReleaseListResponseModel]
)
async def list_releases(request: Request, repo: str) -> JSONResponse:
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

    per_page = int(request.args.get("per_page")) if request.args.get("per_page") else 30
    page = int(request.args.get("page")) if request.args.get("page") else 1

    data: dict[str, list[Release]] = {
        "releases": await github_backend.list_releases(
            repository=GithubRepository(owner=owner, name=repo),
            per_page=per_page,
            page=page,
        )
    }

    return json(data, status=200)


@github.get("/<repo:str>/releases/latest")
@openapi.definition(
    summary="Get the latest release for a repository",
    response=SingleReleaseResponseModel,
)
async def latest_release(request: Request, repo: str) -> JSONResponse:
    """
    Retrieve the latest release for a GitHub repository.

    **Args:**
        - repo (str): The name of the GitHub repository to retrieve the release for.

    **Query Parameters:**
        - dev (bool): Whether or not to retrieve the latest development release.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the release.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data: dict[str, Release]

    match request.args.get("dev"):
        case "true":
            data = {
                "release": await github_backend.get_latest_pre_release(
                    repository=GithubRepository(owner=owner, name=repo)
                )
            }
        case _:
            data = {
                "release": await github_backend.get_latest_release(
                    repository=GithubRepository(owner=owner, name=repo)
                )
            }

    return json(data, status=200)


@github.get("/<repo:str>/releases/tag/<tag:str>")
@openapi.definition(
    summary="Retrieve a release for a GitHub repository by its tag name.",
    response=SingleReleaseResponseModel,
)
async def get_release_by_tag_name(
    request: Request, repo: str, tag: str
) -> JSONResponse:
    """
    Retrieve a release for a GitHub repository by its tag name.

    **Args:**
        - repo (str): The name of the GitHub repository to retrieve the release for.
        - tag (str): The tag for the release to be retrieved.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the release.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data: dict[str, Release] = {
        "release": await github_backend.get_release_by_tag_name(
            repository=GithubRepository(owner=owner, name=repo), tag_name=tag
        )
    }

    return json(data, status=200)


@github.get("/<repo:str>/contributors")
@openapi.definition(
    summary="Retrieve a list of contributors for a repository.",
    response=ContributorsModel,
)
async def get_contributors(request: Request, repo: str) -> JSONResponse:
    """
    Retrieve a list of contributors for a repository.

    **Args:**
        - repo (str): The name of the GitHub repository to retrieve the contributors for.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of contributors.

    **Raises:**
        - HTTPException: If there is an error retrieving the contributors.
    """

    data: dict[str, list[Contributor]] = {
        "contributors": await github_backend.get_contributors(
            repository=GithubRepository(owner=owner, name=repo)
        )
    }

    return json(data, status=200)


@github.get("/patches/<tag:str>")
@openapi.definition(
    summary="Retrieve a list of patches for a release.", response=PatchesModel
)
async def get_patches(request: Request, tag: str) -> JSONResponse:
    """
    Retrieve a list of patches for a release.

    **Args:**
        - tag (str): The tag for the patches to be retrieved.

    **Query Parameters:**
        - dev (bool): Whether or not to retrieve the latest development release.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of patches.

    **Raises:**
        - HTTPException: If there is an error retrieving the patches.
    """

    repo: str = "revanced-patches"

    dev: bool = bool(request.args.get("dev"))

    data: dict[str, list[dict]] = {
        "patches": await github_backend.get_patches(
            repository=GithubRepository(owner=owner, name=repo), tag_name=tag, dev=dev
        )
    }

    return json(data, status=200)


@github.get("/team/members")
@openapi.definition(
    summary="Retrieve a list of team members for the Revanced organization.",
    response=TeamMembersModel,
)
async def get_team_members(request: Request) -> JSONResponse:
    """
    Retrieve a list of team members for the Revanced organization.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the list of team members.

    **Raises:**
        - HTTPException: If there is an error retrieving the team members.
    """

    data: dict[str, list[Contributor]] = {
        "members": await github_backend.get_team_members(
            repository=GithubRepository(owner=owner, name=default_repository)
        )
    }

    return json(data, status=200)
