# api/__init__.py
from sanic import Blueprint

from api.github import github
from api.ping import ping
from api.socials import socials
from api.compat import github as old
from api.donations import donations

api = Blueprint.group(ping, github, socials, donations, old, url_prefix="/")
