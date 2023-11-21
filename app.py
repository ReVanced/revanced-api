# app.py

import os

from sanic import Sanic
import sanic.response
from sanic_ext import Config

from api import api
from config import openapi_title, openapi_version, openapi_description

from limiter import configure_limiter
from auth import configure_auth

import sentry_sdk

if os.environ.get("SENTRY_DSN"):
    sentry_sdk.init(
        dsn=os.environ["SENTRY_DSN"],
        enable_tracing=True,
        traces_sample_rate=1.0,
        debug=True,
    )
else:
    print("WARNING: Sentry DSN not set, not enabling Sentry")

REDIRECTS = {
    "/": "/docs/swagger",
}

app = Sanic("revanced-api")
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

# sanic-beskar
configure_auth(app)

# sanic-limiter
configure_limiter(app)

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
    ] = "default-src  * 'unsafe-inline' 'unsafe-eval' data: blob:;"
