# app.py
from sanic import Sanic

from api import api

app = Sanic("ReVanced-API")
app.blueprint(api)
