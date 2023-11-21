"""
This module provides a blueprint for the connections endpoint.

Routes:
    - GET /connections: Get ReVanced connection links.
"""

import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.models.socials import ConnectionsResponseModel
from config import social_links

connections: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))


@connections.get("/connections")
@openapi.definition(
    summary="Get ReVanced connection links",
    response=[ConnectionsResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing ReVanced connection links.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the connection links.
    """
    data: dict[str, dict] = {"connections": social_links}
    return json(data, status=200)
