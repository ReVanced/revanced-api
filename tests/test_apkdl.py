# import pytest
# from sanic import Sanic

# from api.models.appinfo import AppInfoModel

# from config import api_version, apkdl_testing_package

# # socials


# @pytest.mark.asyncio
# async def test_socials(app: Sanic):
#     _, response = await app.asgi_client.get(
#         f"/{api_version}/app/info/{apkdl_testing_package}"
#     )
#     assert response.status == 200
#     assert AppInfoModel(app_info=response.json["app_info"])
