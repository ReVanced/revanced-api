import pytest
from sanic import Sanic

# ping


@pytest.mark.asyncio
async def test_ping(app: Sanic):
    _, response = await app.asgi_client.head("/v2/ping")
    assert response.status == 204
