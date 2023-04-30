# api/__init__.py
from sanic import Blueprint
from api.ping import ping
from api.socials import socials
from api.github import github

api = Blueprint.group(ping, socials, github, url_prefix="/")
