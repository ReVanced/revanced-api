"""
This module provides ReVanced Manager specific endpoints.

Routes:
    - GET /manager/bootstrap: Get a list of the main ReVanced tools.
    - GET /manager/sources: Get a list of ReVanced sources.
"""

import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.backends.github import GithubRepository, Github

from api.models.manager import BootsrapResponseModel, CustomSourcesResponseModel
from config import compat_repositories, owner

manager: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))

github_backend: Github = Github()


@manager.get("/manager/bootstrap")
@openapi.definition(
    summary="Get a list of the main ReVanced tools",
    response=[BootsrapResponseModel],
)
async def bootstrap(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a list of the main ReVanced tools.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a list with the tool names.
    """
    data: dict[str, dict] = {"tools": compat_repositories}
    return json(data, status=200)


@manager.get("/manager/custom-source")
@openapi.definition(
    summary="Get a list of ReVanced sources",
    response=[CustomSourcesResponseModel],
)
async def custom_sources(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a list of the main ReVanced sources.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a list with the source names.
    """
    data = await github_backend.generate_custom_sources(
        repositories=[
            GithubRepository(owner=owner, name=repo)
            for repo in compat_repositories
            if "patches" in repo or "integrations" in repo
        ],
        dev=True if request.args.get("dev") else False,
    )

    return json(data, status=200)
