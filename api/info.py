"""
This module provides a blueprint for the info endpoint.

Routes:
    - GET /info: Get info about the owner of the API.
"""

import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.models.info import InfoResponseModel
from config import default_info

from loguru import logger
import ujson

from cli import CLI

info: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))
cli = CLI()


@info.get("/info")
@openapi.definition(
    summary="Information about the API",
    response=[InfoResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing info about the owner of the API.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the info about the owner of the API.
    """
    data: dict[str, dict] = {"info": default_info}
    return json(data, status=200)


@cli.helper(name="info")
async def cli_root() -> dict[str, dict]:
    """
    Returns a JSONResponse with a dictionary containing info about the owner of the API.

    Returns:
        - dict: A dictionary with the info about the owner of the API.
    """

    path = cli.get_file_path("/info")

    data = {"info": default_info}

    if path:
        with open(f"{path}/index.json", "w") as file:
            ujson.dump(data, file)
    else:
        logger.warning(
            "Could not find path for info. Did you generate the scaffolding?"
        )

    return ujson.dumps(data)
