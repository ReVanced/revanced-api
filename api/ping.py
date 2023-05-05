from sanic import Blueprint
from sanic import HTTPResponse
from sanic import Request
from sanic import response
from sanic_ext import openapi

"""
This module provides endpoints for pinging the API.

Routes:
    - HEAD /ping: Ping the API.
"""

ping: Blueprint = Blueprint("ping", version=2)


@ping.head("/ping")
@openapi.summary("Ping the API")
async def root(request: Request) -> HTTPResponse:
    """
    Endpoint for pinging the API.

    **Returns:**
        - Empty response with status code 204.
    """
    return response.empty(status=204)
