"""
This module provides a blueprint for the socials endpoint.

Routes:
    - GET /socials: Get ReVanced socials.
"""

import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from loguru import logger

import ujson

from api.models.socials import SocialsResponseModel
from config import social_links

from cli import CLI

socials: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))
cli = CLI()


@socials.get("/socials")
@openapi.definition(
    summary="Get ReVanced socials",
    response=[SocialsResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing ReVanced social links.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the social links.
    """
    data: dict[str, dict] = {"socials": social_links}
    return json(data, status=200)


@cli.helper(name="socials")
async def cli_root() -> dict[str, dict]:
    """
    Returns a dictionary containing ReVanced social links.

    Returns:
        - dict: A dictionary with the social links.
    """

    path = cli.get_file_path("/socials")

    data = {"socials": social_links}

    if path:
        with open(f"{path}/index.json", "w") as file:
            ujson.dump(data, file)
    else:
        logger.warning(
            "Could not find path for socials. Did you generate the scaffolding?"
        )

    return ujson.dumps(data)
