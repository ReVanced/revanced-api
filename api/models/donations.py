from pydantic import BaseModel


class DonationFields(BaseModel):
    """
    A Pydantic BaseModel that represents all the donation links and wallets.
    """

    wallets: dict[str, str]
    links: dict[str, str]


class DonationsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of donation links.
    """

    donations: DonationFields
