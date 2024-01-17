"""
This module provides endpoints for pinging the API.

Routes:
    - GET /ping: Ping the API.
"""

import os
from sanic import Blueprint, HTTPResponse, Request, response
from sanic_ext import openapi

ping: Blueprint = Blueprint(os.path.basename(__file__).rstrip(".py"))


@ping.get("/ping")
@openapi.summary("Ping the API")
async def root(request: Request) -> HTTPResponse:
    """
    Endpoint for pinging the API.

    **Returns:**
        - Empty response with status code 204.
    """
    return response.empty(status=204)
