from pydantic import BaseModel


class WalletFields(BaseModel):
    """
    Implements the fields for a crypto wallet.
    """

    network: str
    currency_code: str
    address: str


class LinkFields(BaseModel):
    """
    Implements the fields for a donation link.
    """

    name: str
    url: str


class DonationFields(BaseModel):
    """
    A Pydantic BaseModel that represents all the donation links and wallets.
    """

    wallets: list[WalletFields]
    links: list[LinkFields]


class DonationsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of donation links.
    """

    donations: DonationFields
