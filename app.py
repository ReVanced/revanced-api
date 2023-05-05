# app.py
from sanic import Sanic
from sanic_ext import Config

from api import api

app = Sanic("ReVanced-API")
app.extend(config=Config(oas_ignore_head=False))
app.blueprint(api)
