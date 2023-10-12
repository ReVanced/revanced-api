import pytest
from sanic import Sanic

from api.models.branding import BrandingResponseModel

from config import api_version

# branding


@pytest.mark.asyncio
async def test_branding(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/branding")
    assert response.status == 200
    assert BrandingResponseModel(**response.json)
