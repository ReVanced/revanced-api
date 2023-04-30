from dataclasses import dataclass


@dataclass
class Metadata(dict):
    def __init__(
        self,
        tag_name: str,
        name: str,
        body: str,
        draft: bool,
        prerelease: bool,
        created_at: str,
        published_at: str,
    ):
        dict.__init__(
            self,
            tag_name=tag_name,
            name=name,
            body=body,
            draft=draft,
            prerelease=prerelease,
            created_at=created_at,
            published_at=published_at,
        )


@dataclass
class Asset(dict):
    def __init__(self, name: str, content_type: str, download_url: str):
        dict.__init__(
            self, name=name, content_type=content_type, download_url=download_url
        )


@dataclass
class Release(dict):
    def __init__(self, metadata: Metadata, assets: list[Asset]):
        dict.__init__(self, metadata=metadata, assets=assets)


@dataclass
class Notes(dict):
    def __init__(self, name: str, body: str):
        dict.__init__(self, name=name, body=body)


@dataclass
class Contributor(dict):
    def __init__(self, login: str, avatar_url: str, html_url: str, contributions: int):
        dict.__init__(
            self,
            login=login,
            avatar_url=avatar_url,
            html_url=html_url,
            contributions=contributions,
        )
