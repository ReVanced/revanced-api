import pytest
from sanic import Sanic

from config import api_version

# ping


@pytest.mark.asyncio
async def test_ping(app: Sanic):
    _, response = await app.asgi_client.head(f"/{api_version}/ping")
    assert response.status == 204
