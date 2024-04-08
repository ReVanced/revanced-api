from pydantic import BaseModel


class SocialFields(BaseModel):
    """
    Implements the fields for a social network link.
    """

    name: str
    url: str
    icon_light: str
    icon_dark: str
    preferred: bool


class SocialsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of social links.
    """

    socials: list[SocialFields]
    """
    A dictionary where the keys are the names of the social networks, and
    the values are the links to the profiles or pages.
    """
