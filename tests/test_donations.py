import pytest
from sanic import Sanic

from api.models.donations import DonationsResponseModel

from config import api_version

# donations


@pytest.mark.asyncio
async def test_donations(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/donations")
    assert response.status == 200
    assert DonationsResponseModel(**response.json)
