"""
This module provides a blueprint for the app endpoint.

Routes:
    - GET /app/info: Get app info.
"""

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi

from api.backends.apkdl import ApkDl
from api.backends.entities import AppInfo
from api.models.appinfo import AppInfoModel

apkdl: Blueprint = Blueprint("app", version=1)

apkdl_backend: ApkDl = ApkDl()


@apkdl.get("/app/info/<app_id:str>")
@openapi.definition(
    summary="Get information about an app",
    response=[AppInfoModel],
)
async def root(request: Request, app_id: str) -> JSONResponse:
    data: dict[str, AppInfo] = {}
    data["app_info"] = await apkdl_backend.get_app_info(package_name=app_id)
    return json(data, status=200)
