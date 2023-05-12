import asyncio

import ujson
import base64
from aiohttp import ClientResponse
from bs4 import BeautifulSoup
from sanic import SanicException

from api.backends.backend import AppInfoProvider
from api.backends.entities import AppInfo
from api.utils.http_utils import http_get

name: str = "apkdl"
base_url: str = "https://apk-dl.com"


class ApkDl(AppInfoProvider):
    def __init__(self):
        super().__init__(name, base_url)

    async def get_app_info(self, package_name: str) -> AppInfo:
        app_url: str = f"{base_url}/{package_name}"
        response: ClientResponse = await http_get(headers={}, url=app_url)
        if response.status == 200:
            page = BeautifulSoup(await response.read(), "lxml")
            app_name: str = (
                page.find("div", text="App Name").find_next_sibling("div").text
            )
            category: str = (
                page.find("div", text="Category").find_next_sibling("div").text
            )
            logo_url: str = page.find("div", {"class": "logo"}).img["src"]
            logo_response: ClientResponse = await http_get(headers={}, url=logo_url)
            logo_string: str
            if logo_response.status == 200:
                encoded_logo: str = base64.b64encode(
                    await logo_response.content.read()
                ).decode("utf-8")
                logo_string = f"data:image/png;base64,{encoded_logo}"
            else:
                logo_string = ""
            return AppInfo(
                name=app_name,
                category=category,
                logo=logo_string,
            )
        else:
            raise SanicException(f"ApkDl: {response.status} {response.reason}")
