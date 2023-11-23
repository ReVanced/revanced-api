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

from loguru import logger
import ujson

from cli import CLI

manager: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))
cli = CLI()
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


@cli.helper(name="manager")
async def cli_root(bootstrap=None, custom_source=None, dev=False) -> dict[str, dict]:
    """
    Returns a dictionary containing a list of the main ReVanced tools.

    Arguments:
        - bootstrap (bool): Whether to return the bootstrap or not.
        - custom_source (bool): Whether to return the custom source or not.

    Returns:
        - dict: A dictionary with the tool names.
    """

    if not bootstrap and not custom_source:
        bootstrap = True
        custom_source = True

    data = []

    if bootstrap:
        path = cli.get_file_path("/manager/bootstrap")
        bootstrap_data = {"tools": compat_repositories}
        data.append(bootstrap_data)

        if path:
            with open(f"{path}/index.json", "w") as file:
                ujson.dump(bootstrap_data, file)
        else:
            logger.warning(
                "Could not find path for manager/bootsrap/. Did you generate the scaffolding?"
            )

    if custom_source:
        path = cli.get_file_path("/manager/custom-source")
        custom_source_data = await github_backend.generate_custom_sources(
            repositories=[
                GithubRepository(owner=owner, name=repo)
                for repo in compat_repositories
                if "patches" in repo or "integrations" in repo
            ],
            dev=dev,
        )
        data.append(custom_source_data)

        if path:
            with open(f"{path}/index.json", "w") as file:
                ujson.dump(custom_source_data, file)
        else:
            logger.warning(
                "Could not find path for manager/custom-source. Did you generate the scaffolding?"
            )

    return ujson.dumps(data)
