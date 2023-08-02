from pydantic import BaseModel

class SocialField(BaseModel):
    """
    Implements the fields for a social network link.
    """

    name: str
    url: str

class SocialsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of social links.
    """

    socials: list[SocialField]
    """
    A dictionary where the keys are the names of the social networks, and
    the values are the links to the profiles or pages.
    """
