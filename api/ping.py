"""
This module provides endpoints for pinging the API.

Routes:
    - HEAD /ping: Ping the API.
"""

from sanic import Blueprint, HTTPResponse, Request, response
from sanic_ext import openapi
from config import api_version

ping: Blueprint = Blueprint("ping", version=api_version)


@ping.head("/ping")
@openapi.summary("Ping the API")
async def root(request: Request) -> HTTPResponse:
    """
    Endpoint for pinging the API.

    **Returns:**
        - Empty response with status code 204.
    """
    return response.empty(status=204)
