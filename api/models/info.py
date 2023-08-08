from api.models.donations import DonationFields
from api.models.socials import SocialFields
from pydantic import BaseModel

class InfoFields(BaseModel):
    """
    Implements the fields for a API owner info.
    """
    name: str
    about: str
    contact: dict[str, str]
    socials: SocialFields
    donations: DonationFields


class InfoResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of info.
    """

    info: InfoFields
    