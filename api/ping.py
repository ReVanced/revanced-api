from sanic import Blueprint, HTTPResponse, Request, response
from sanic_ext import openapi

ping: Blueprint = Blueprint("ping", version=1)


@ping.route("/ping")
@openapi.summary("Ping the API")
async def root(request: Request, methods: list[str] = ["HEAD"]) -> HTTPResponse:
    return response.empty(status=204)
