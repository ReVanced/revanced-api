"""
This module provides a blueprint for the branding endpoint.

Routes:
    - GET /branding: Get ReVanced branding.
"""

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.models.branding import BrandingResponseModel
from config import branding_links, api_version

branding: Blueprint = Blueprint("branding", version=api_version)


@branding.get("/branding")
@openapi.definition(
    summary="Get ReVanced branding",
    response=[BrandingResponseModel],
)
async def root(request: Request) -> JSONResponse:
    """
    Returns a JSONResponse with a dictionary containing ReVanced branding links.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse instance containing a dictionary with the branding links.
    """
    data: dict[str, dict] = {"branding": branding_links}
    return json(data, status=200)
