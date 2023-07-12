import pytest
from sanic import Sanic
from sanic_testing.testing import TestingResponse

from api.models.github import (
    AssetFields,
    MetadataFields,
    PatchesResponseFields,
    ReleaseListResponseModel,
    ReleaseResponseModel,
    SingleReleaseResponseModel,
    ContributorsFields,
    ContributorsModel,
    PatchesModel,
    TeamMemberFields,
    TeamMembersModel,
)

from config import github_testing_repository, github_testing_tag, api_version


# utils


async def __test_single_release(response: TestingResponse) -> bool:
    try:
        assert response.status == 200
        assert SingleReleaseResponseModel(
            release=ReleaseResponseModel(
                metadata=MetadataFields(**response.json["release"]["metadata"]),
                assets=[
                    AssetFields(**asset) for asset in response.json["release"]["assets"]
                ],
            )
        )
        return True
    except AssertionError:
        return False


# github


@pytest.mark.asyncio
async def test_releases(app: Sanic):
    _, response = await app.asgi_client.get(
        f"/{api_version}/{github_testing_repository}/releases"
    )
    assert response.status == 200
    assert ReleaseListResponseModel(
        releases=[
            ReleaseResponseModel(
                metadata=MetadataFields(**release["metadata"]),
                assets=[AssetFields(**asset) for asset in release["assets"]],
            )
            for release in response.json["releases"]
        ]
    )


@pytest.mark.asyncio
async def test_latest_release(app: Sanic):
    _, response = await app.asgi_client.get(
        f"/{api_version}/{github_testing_repository}/releases/latest"
    )
    _, response_dev = await app.asgi_client.get(
        f"/{api_version}/{github_testing_repository}/releases/latest?dev=true"
    )
    assert await __test_single_release(response)
    assert await __test_single_release(response_dev)


@pytest.mark.asyncio
async def test_release_by_tag(app: Sanic):
    _, response = await app.asgi_client.get(
        f"/{api_version}/{github_testing_repository}/releases/tag/{github_testing_tag}"
    )
    assert await __test_single_release(response)


@pytest.mark.asyncio
async def test_contributors(app: Sanic):
    _, response = await app.asgi_client.get(
        f"/{api_version}/{github_testing_repository}/contributors"
    )
    assert ContributorsModel(
        contributors=[
            ContributorsFields(**contributor)
            for contributor in response.json["contributors"]
        ]
    )


@pytest.mark.asyncio
async def test_patches(app: Sanic):
    _, response = await app.asgi_client.get(
        f"/{api_version}/patches/{github_testing_tag}"
    )

    assert PatchesModel(
        patches=[PatchesResponseFields(**patch) for patch in response.json["patches"]]
    )


@pytest.mark.asyncio
async def test_team_members(app: Sanic):
    _, response = await app.asgi_client.get(f"/{api_version}/team/members")
    assert TeamMembersModel(
        members=[TeamMemberFields(**member) for member in response.json["members"]]
    )
