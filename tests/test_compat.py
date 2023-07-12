import pytest
from sanic import Sanic

from api.models.compat import ToolsResponseModel

# compatibility layer


@pytest.mark.asyncio
async def test_compat_tools(app: Sanic):
    _, response = await app.asgi_client.get(f"/tools")
    assert response.status == 200
    assert ToolsResponseModel(tools=[tool for tool in response.json["tools"]])
