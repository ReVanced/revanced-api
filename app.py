# app.py

from sanic import Sanic
from sanic_ext import Config

from api import api
from config import *

app = Sanic("ReVanced-API")
app.extend(config=Config(oas_ignore_head=False))
app.ext.openapi.describe(
    title=openapi_title,
    version=openapi_version,
    description=openapi_description,
)
app.blueprint(api)
