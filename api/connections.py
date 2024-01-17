"""
This module provides a blueprint for the connections endpoint.

Routes:
    - GET /connections: Get ReVanced connections.
"""

import os
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.models.connections import ConnectionsResponseModel
from config import connection_links

connections: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))


@connections.get("/connections")
@openapi.definition(
    summary="Get ReVanced's connections",
    response=[ConnectionsResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing ReVanced connections.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the connections.
    """
    data: dict[str, dict] = {"connections": connection_links}
    return json(data, status=200)
