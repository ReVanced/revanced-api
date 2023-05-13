import pytest
from sanic import Sanic

from api.models.socials import SocialsResponseModel

from config import api_version

# socials


@pytest.mark.asyncio
async def test_socials(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/socials")
    assert response.status == 200
    assert SocialsResponseModel(**response.json)
