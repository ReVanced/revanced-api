import asyncio

import pytest
from sanic import Sanic

from api import api


@pytest.fixture
def app() -> Sanic:
    app: Sanic = Sanic("ReVanced-API")
    app.blueprint(api)
    app.config.TOUCHUP = False
    return app


@pytest.fixture(scope="session")
def event_loop():
    try:
        loop = asyncio.get_running_loop()
    except RuntimeError:
        loop = asyncio.new_event_loop()
    yield loop
    loop.close()
