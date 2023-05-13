import pytest
from sanic import Sanic

from api.models.socials import SocialsResponseModel

# socials


@pytest.mark.asyncio
async def test_socials(app: Sanic):
    _, response = await app.asgi_client.get("/v2/socials")
    assert response.status == 200
    assert SocialsResponseModel(**response.json)
