from sanic import Blueprint
from sanic import Request
from sanic.response import json
from sanic.response import JSONResponse
from sanic_ext import openapi

from api.models.socials import Socials
from config import social_links

socials: Blueprint = Blueprint("socials", version=1)


@socials.route("/socials")
@openapi.definition(
    summary="Get ReVanced socials",
    response=[Socials],
)
async def root(request: Request, methods: list[str] = ["GET"]) -> JSONResponse:
    data: dict[str, dict] = {}
    data["socials"] = social_links
    return json(data, status=200)
