from api.models.branding import BrandingFields
from api.models.donations import DonationFields
from api.models.socials import SocialFields
from pydantic import BaseModel


class ContactFields(BaseModel):
    """
    Implements the fields for the API owner contact info.
    """

    email: str


class InfoFields(BaseModel):
    """
    Implements the fields for the API owner info.
    """

    name: str
    about: str
    contact: ContactFields
    branding: list[BrandingFields]
    socials: list[SocialFields]
    donations: DonationFields


class InfoResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of info.
    """

    info: InfoFields
