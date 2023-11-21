"""
This module provides a blueprint for the socials endpoint.

Routes:
    - GET /socials: Get ReVanced socials.
"""

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.models.socials import SocialsResponseModel
from config import social_links, api_version

socials: Blueprint = Blueprint("socials", version=api_version)

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
