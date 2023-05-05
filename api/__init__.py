# api/__init__.py
from sanic import Blueprint

from api.github import github
from api.ping import ping
from api.socials import socials

api = Blueprint.group(ping, socials, github, url_prefix="/")
