from sanic import Blueprint
from sanic import HTTPResponse
from sanic import Request
from sanic import response
from sanic_ext import openapi

ping: Blueprint = Blueprint("ping", version=1)


@ping.route("/ping")
@openapi.summary("Ping the API")
async def root(request: Request, methods: list[str] = ["HEAD"]) -> HTTPResponse:
    return response.empty(status=204)
