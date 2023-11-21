"""
This module provides endpoints for pinging the API.

Routes:
    - HEAD /ping: Ping the API.
"""

from sanic import Blueprint, HTTPResponse, Request, response
from sanic_ext import openapi
from api.utils.versioning import get_version

module_name = "ping"
ping: Blueprint = Blueprint(module_name, version=get_version(module_name))


@ping.head("/ping")
@openapi.summary("Ping the API")
async def root(request: Request) -> HTTPResponse:
    """
    Endpoint for pinging the API.

    **Returns:**
        - Empty response with status code 204.
    """
    return response.empty(status=204)
