from pydantic import BaseModel


class BootsrapResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a list of available tools.
    """

    tools: list[str]
    """
    A list of available tools.
    """


class CustomSourcesFields(BaseModel):
    """
    Implements the fields for a source.
    """

    url: str
    preferred: bool


class CustomSourcesResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a list of available sources.
    """

    _: dict[str, CustomSourcesFields]
    """
    A list of available sources.
    """
