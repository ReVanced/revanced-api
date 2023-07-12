# app.py
from sanic import Sanic
import sanic.response
from sanic_ext import Config

from api import api
from config import *

REDIRECTS = {
    "/": "/docs/swagger",
}

app = Sanic("ReVanced-API")
app.extend(config=Config(oas_ignore_head=False))
app.ext.openapi.describe(
    title=openapi_title,
    version=openapi_version,
    description=openapi_description,
)
app.config.CORS_ALWAYS_SEND = True
app.config.CORS_AUTOMATIC_OPTIONS = True
app.config.CORS_VARY_HEADER = True
app.config.CORS_METHODS = ["GET", "HEAD", "OPTIONS"]
app.config.CORS_SUPPORTS_CREDENTIALS = True
app.config.CORS_SEND_WILDCARD = True
app.config.CORS_ORIGINS = "*"

app.blueprint(api)


# https://sanic.dev/en/guide/how-to/static-redirects.html


def get_static_function(value):
    return lambda *_, **__: value


for src, dest in REDIRECTS.items():
    app.route(src)(get_static_function(sanic.response.redirect(dest)))


@app.middleware("response")
async def add_cache_control(request, response):
    response.headers["Cache-Control"] = "public, max-age=300"


@app.middleware("response")
async def add_csp(request, response):
    response.headers[
        "Content-Security-Policy"
    ] = "default-src * 'unsafe-inline' 'unsafe-eval'; img-src * data:;"
