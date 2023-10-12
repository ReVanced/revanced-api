from pydantic import BaseModel


class BrandingFields(BaseModel):
    """
    Implements the fields for a brand link.
    """

    assettype: str
    url: str


class BrandingResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of branding links.
    """

    branding: list[BrandingFields]
