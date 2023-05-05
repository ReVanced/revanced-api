from sanic import Blueprint
from sanic import Request
from sanic.response import json
from sanic.response import JSONResponse
from sanic_ext import openapi

<<<<<<< HEAD
from api.backends.github import Github
from api.backends.github import GithubRepository
=======
from api.backends.github import Github, GithubRepository
from api.models.github import *
from config import owner
>>>>>>> 93af5f0 (refactor: backend changes)

"""
This module provides endpoints for interacting with the Github API.

Routes:
    - GET /<repo:str>/releases: Retrieve a list of releases for a Github repository.
    - GET /<repo:str>/releases/latest: Retrieve the latest release for a Github repository.

"""

github: Blueprint = Blueprint('github', version=2)

github_backend: Github = Github()


@github.get('/<repo:str>/releases')
@openapi.definition(
    summary='Get releases for a repository', response=[ReleaseListResponseModel]
)
async def list_releases(request: Request, repo: str) -> JSONResponse:
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

    per_page = int(request.args.get('per_page')) if request.args.get('per_page') else 30
    page = int(request.args.get('page')) if request.args.get('page') else 1

    data = await github_backend.list_releases(
        repository=GithubRepository(owner=owner, name=repo),
        per_page=per_page,
        page=page,
    )

    return json(data, status=200)


@github.get('/<repo:str>/release/latest')
@openapi.definition(
    summary='Get the latest release for a repository', response=ReleaseResponseModel
)
async def latest_release(request: Request, repo: str) -> JSONResponse:
    """
    Retrieve the latest release for a Github repository.

    **Args:**
        - repo (str): The name of the Github repository to retrieve the release for.

    **Query Parameters:**
        - dev (bool): Whether or not to retrieve the latest development release.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the release.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data = await github_backend.get_latest_release(
        repository=GithubRepository(owner=owner, name=repo)
    )

    return json(data, status=200)


@github.get('/<repo:str>/release/tag/<tag:str>')
@openapi.definition(
    summary='Retrieve a release for a Github repository by its tag name.',
    response=ReleaseResponseModel,
)
async def get_release_by_tag_name(
    request: Request, repo: str, tag: str
) -> JSONResponse:
    """
    Retrieve a release for a Github repository by its tag name.

    **Args:**
        - repo (str): The name of the Github repository to retrieve the release for.
        - repo (str): The tag for the release to be retrieved.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the release.

    **Raises:**
        - HTTPException: If there is an error retrieving the releases.
    """

    data = await github_backend.get_release_by_tag_name(
        repository=GithubRepository(owner=owner, name=repo), tag_name=tag
    )

    return json(data, status=200)
