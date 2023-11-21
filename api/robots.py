import os
from sanic import Blueprint
from sanic.response import text


robots: Blueprint = Blueprint(os.path.basename(__file__).strip(".py"))


@robots.get("/robots.txt")
async def robots_txt(request):
    return text("User-agent: *\nDisallow: /", content_type="text/plain")
