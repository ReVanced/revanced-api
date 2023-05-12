from dataclasses import dataclass


@dataclass
class Metadata(dict):
    """
    Represents the metadata of a release.

    Attributes:
        - tag_name (str): The name of the release tag.
        - name (str): The name of the release.
        - body (str): The body of the release.
        - draft (bool): Whether the release is a draft.
        - prerelease (bool): Whether the release is a prerelease.
        - created_at (str): The creation date of the release.
        - published_at (str): The publication date of the release.
    """

    def __init__(
        self,
        tag_name: str,
        name: str,
        draft: bool,
        prerelease: bool,
        created_at: str,
        published_at: str,
        body: str,
    ):
        dict.__init__(
            self,
            tag_name=tag_name,
            name=name,
            draft=draft,
            prerelease=prerelease,
            created_at=created_at,
            published_at=published_at,
            body=body,
        )


@dataclass
class Asset(dict):
    """
    Represents an asset in a release.

    Attributes:
        - name (str): The name of the asset.
        - content_type (str): The MIME type of the asset content.
        - download_url (str): The URL to download the asset.
    """

    def __init__(self, name: str, content_type: str, browser_download_url: str):
        dict.__init__(
            self,
            name=name,
            content_type=content_type,
            browser_download_url=browser_download_url,
        )


@dataclass
class Release(dict):
    """
    Represents a release.

    Attributes:
        - metadata (Metadata): The metadata of the release.
        - assets (list[Asset]): The assets of the release.
    """

    def __init__(self, metadata: Metadata, assets: list[Asset]):
        dict.__init__(self, metadata=metadata, assets=assets)


@dataclass
class Contributor(dict):
    """
    Represents a contributor to a repository.

    Attributes:
        - login (str): The GitHub username of the contributor.
        - avatar_url (str): The URL to the contributor's avatar image.
        - html_url (str): The URL to the contributor's GitHub profile.
        - contributions (int): The number of contributions the contributor has made to the repository.
    """

    def __init__(self, login: str, avatar_url: str, html_url: str, contributions: int):
        dict.__init__(
            self,
            login=login,
            avatar_url=avatar_url,
            html_url=html_url,
            contributions=contributions,
        )


@dataclass
class AppInfo(dict):
    """
    Represents the information of an app.

    Attributes:
        - name (str): The name of the app.
        - category (str): The app category.
        - logo (str): The base64 enconded app logo.
    """

    def __init__(self, name: str, category: str, logo: str):
        dict.__init__(
            self,
            name=name,
            category=category,
            logo=logo,
        )
