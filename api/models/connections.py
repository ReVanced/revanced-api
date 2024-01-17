from pydantic import BaseModel


class ConnectionFields(BaseModel):
    """
    Implements the fields for a connection.
    """

    name: str
    url: str
    preferred: bool


class ConnectionsResponseModel(BaseModel):
    """
    A Pydantic BaseModel that represents a dictionary of connection.
    """

    connections: list[ConnectionFields]
    """
    A dictionary where the keys are the names of the connections, and
    the values are the links to the profiles or pages.
    """
