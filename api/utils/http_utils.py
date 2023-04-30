from typing import Optional

import ujson
from aiohttp import ClientSession

_client: Optional[ClientSession] = None


async def http_get(headers, url):
    global _client
    if _client == None:
        _client = ClientSession(json_serialize=ujson.dumps)
    return await _client.get(url, headers=headers)
