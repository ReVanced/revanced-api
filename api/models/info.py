from api.models.donations import DonationFields
from api.models.connections import ConnectionFields
from pydantic import BaseModel


class ContactFields(BaseModel):
    """
    Implements the fields for the API owner contact info.
    """

    email: str


class BrandingFields(BaseModel):
    """
    Implements the fields for the API owner branding info.
    """

    logo: str


class InfoFields(BaseModel):
    """
    Implements the fields for the API owner info.
    """

    name: str
    about: str
    branding: BrandingFields
    contact: ContactFields
    connections: list[ConnectionFields]
    donations: DonationFields


class InfoResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of info.
    """

    info: InfoFields
