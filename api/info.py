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

info: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))


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
