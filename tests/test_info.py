import pytest
from sanic import Sanic

from api.models.info import InfoResponseModel

from config import api_version

# info


@pytest.mark.asyncio
async def test_info(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/info")
    assert response.status == 200
    print(response.json)
    assert InfoResponseModel(**response.json)
