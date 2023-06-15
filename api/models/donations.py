from pydantic import BaseModel


class DonationsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of donation links.
    """

    donations: dict[str, str]
    """
    A dictionary where the keys are the names of the donation destinations, and
    the values are the links to services or wallet addresses.
    """
