# api/__init__.py
from sanic import Blueprint

from api.github import github
from api.ping import ping
from api.socials import socials
from api.info import info
from api.compat import github as compat
from api.donations import donations
from api.announcements import announcements
from api.login import login

api = Blueprint.group(login, ping, github, info, socials,
                      donations, announcements, compat, url_prefix="/")
