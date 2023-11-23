"""
This module provides a blueprint for the donations endpoint.

Routes:
    - GET /donations: Get ReVanced donation links and wallets.
"""

import os

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from loguru import logger
import ujson

from api.models.donations import DonationsResponseModel
from config import wallets, links

from cli import CLI

donations: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))
cli = CLI()


@donations.get("/donations")
@openapi.definition(
    summary="Get ReVanced donation links and wallets",
    response=[DonationsResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing ReVanced donation links and wallets.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the donation links and wallets.
    """
    data: dict[str, dict] = {
        "donations": {
            "wallets": wallets,
            "links": links,
        }
    }
    return json(data, status=200)


@cli.helper(name="donations")
async def cli_root() -> dict[str, dict]:
    """
    Returns a dictionary containing ReVanced donation links and wallets.

    Returns:
        - dict: A dictionary with the donation links and wallets.
    """

    path = cli.get_file_path("/donations")

    data = {
        "donations": {
            "wallets": wallets,
            "links": links,
        }
    }

    if path:
        with open(f"{path}/index.json", "w") as file:
            ujson.dump(data, file)
    else:
        logger.warning(
            "Could not find path for donations. Did you generate the scaffolding?"
        )

    return ujson.dumps(data)
