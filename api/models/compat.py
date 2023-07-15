from pydantic import BaseModel
from api.models.github import ContributorsFields


class ToolsResponseFields(BaseModel):
    """Implements the fields for the /tools endpoint.

    Args:
        BaseModel (pydantic.BaseModel): BaseModel from pydantic
    """

    repository: str
    version: str
    timestamp: str
    name: str
    size: str | None = None
    browser_download_url: str
    content_type: str


class ToolsResponseModel(BaseModel):
    """Implements the JSON response model for the /tools endpoint.

    Args:
        BaseModel (pydantic.BaseModel): BaseModel from pydantic
    """

    tools: list[ToolsResponseFields]


class ContributorsResponseFields(BaseModel):
    """Implements the fields for the /contributors endpoint.

    Args:
        BaseModel (pydantic.BaseModel): BaseModel from pydantic
    """

    name: str
    contributors: list[ContributorsFields]


class ContributorsResponseModel(BaseModel):
    """Implements the JSON response model for the /contributors endpoint.

    Args:
        BaseModel (pydantic.BaseModel): BaseModel from pydantic
    """

    repositories: list[ContributorsResponseFields]
