from typing import Optional

import ujson
from aiohttp import ClientSession

_client: Optional[ClientSession] = None


async def http_get(headers, url):
    """
    Performs a GET HTTP request to a given URL with the provided headers.

    Args:
        headers (dict): A dictionary containing headers to be included in the HTTP request.
        url (str): The URL to which the HTTP request will be made.

    Returns:
        The HTTP response returned by the server.
    """
    global _client
    if _client == None:
        _client = ClientSession(json_serialize=ujson.dumps)
        return await _client.get(url, headers=headers)
    else:
        assert isinstance(_client, ClientSession)
        return await _client.get(url, headers=headers)
