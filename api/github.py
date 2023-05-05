from sanic import Blueprint
from sanic import Request
from sanic.response import json
from sanic.response import JSONResponse
from sanic_ext import openapi

from api.backends.github import Github
from api.backends.github import GithubRepository

github: Blueprint = Blueprint("github", url_prefix="/github", version=1)

github_backend: Github = Github()


@github.route("/<owner:str>/<repo:str>/releases", methods=["GET"])
@openapi.definition(
    summary="Get releases for a repository",
)
async def list_releases(
    request: Request, owner: str, repo: str, methods: list[str] = ["GET"]
) -> JSONResponse:
    per_page: int
    page: int

    if request.args.get("per_page"):
        per_page = int(request.args.get("per_page"))
    else:
        per_page = 30

    if request.args.get("page"):
        page = int(request.args.get("page"))
    else:
        page = 1

    repository: GithubRepository = GithubRepository(owner=owner, name=repo)

    data = await github_backend.list_releases(
        repository=repository, per_page=per_page, page=page
    )

    return json(data, status=200)
