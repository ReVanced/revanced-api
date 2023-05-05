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


class AssetFields(BaseModel):
    """
    Asset fields for a GitHub release.
    """

    name: str
    content_type: str
    download_url: str


class ReleaseResponseModel(BaseModel):
    """
    Response model for a GitHub release.
    """

    metadata: MetadataFields
    asset: AssetFields


class ReleaseListResponseModel(BaseModel):
    """
    Response model for a list of GitHub releases.
    """

    releases: list[ReleaseResponseModel]
