from pydantic import BaseModel


class SocialsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of social links.
    """

    socials: dict[str, str]
    """
    A dictionary where the keys are the names of the social networks, and
    the values are the links to the profiles or pages.
    """
