import pytest
from sanic import Sanic

from api.models.connections import ConnectionsResponseModel

from config import api_version

# connections


@pytest.mark.asyncio
async def test_connections(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/connections")
    assert response.status == 200
    assert ConnectionsResponseModel(**response.json)
