"""
This module provides a blueprint for the login endpoint.

Routes:
    - POST /login: Login to the API
"""

from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi
from sanic_beskar.exceptions import AuthenticationError

from api.utils.auth import beskar
from api.utils.limiter import limiter

from api.utils.versioning import get_version

module_name = "login"
login: Blueprint = Blueprint(module_name, version=get_version(module_name))


@login.post("/login")
@openapi.definition(
    summary="Login to the API",
)
@limiter.limit("3 per hour")
async def login_user(request: Request) -> JSONResponse:
    """
    Login to the API.

    **Args:**
        - username (str): The username of the user to login.
        - password (str): The password of the user to login.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the access token.
    """

    req = request.json
    username = req.get("username", None)
    password = req.get("password", None)
    if not username or not password:
        return json({"error": "Missing username or password"}, status=400)

    try:
        user = await beskar.authenticate(username, password)
    except AuthenticationError:
        return json({"error": "Invalid username or password"}, status=403)

    if not user:
        return json({"error": "Invalid username or password"}, status=403)

    ret = {"access_token": await beskar.encode_token(user)}
    return json(ret, status=200)
