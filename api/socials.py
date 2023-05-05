from sanic import Blueprint
from sanic import Request
from sanic.response import json
from sanic.response import JSONResponse
from sanic_ext import openapi

from api.models.socials import SocialsResponseModel
from config import social_links

"""
This module provides a blueprint for the socials endpoint.

Routes:
    - GET /socials: Get ReVanced socials.
"""

socials: Blueprint = Blueprint("socials", version=2)


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
    data: dict[str, dict] = {}
    data["socials"] = social_links
    return json(data, status=200)
