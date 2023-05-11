from typing import Any
from pydantic import BaseModel


class MetadataFields(BaseModel):
    """
    Metadata fields for a GitHub release.
    """

    tag_name: str
    name: str
    draft: bool
    prerelease: bool
    created_at: str
    published_at: str
    body: str


class AssetFields(BaseModel):
    """
    Asset fields for a GitHub release.
    """

    name: str
    content_type: str
    browser_download_url: str


class ReleaseResponseModel(BaseModel):
    """
    Response model for a GitHub release.
    """

    metadata: MetadataFields
    asset: AssetFields


class SingleReleaseResponseModel(BaseModel):
    """
    Response model for a GitHub release.
    """

    release: ReleaseResponseModel


class ReleaseListResponseModel(BaseModel):
    """
    Response model for a list of GitHub releases.
    """

    releases: list[ReleaseResponseModel]


class CompatiblePackagesResponseFields(BaseModel):
    """
    Implements the fields for compatible packages in the PatchesResponseFields class.
    """

    name: str
    versions: list[str] | None


class PatchesOptionsResponseFields(BaseModel):
    key: str
    title: str
    description: str
    required: bool
    choices: list[Any] | None


class PatchesResponseFields(BaseModel):
    """
    Implements the fields for the /patches endpoint.
    """

    name: str
    description: str
    version: str
    excluded: bool
    dependencies: list[str] | None
    options: list[PatchesOptionsResponseFields] | None
    compatiblePackages: list[CompatiblePackagesResponseFields]


class PatchesModel(BaseModel):
    """
    Response model for a list of GitHub releases.
    """

    patches: list[PatchesResponseFields]


class ContributorsFields(BaseModel):
    """
    Implements the fields for a contributor.
    """

    login: str
    avatar_url: str
    html_url: str
    contributions: int


class ContributorsModel(BaseModel):
    """
    Response model for a list of contributors.
    """

    contributors: list[ContributorsFields]
