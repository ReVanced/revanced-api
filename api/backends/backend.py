from abc import abstractmethod
from typing import Any, Protocol

from api.backends.entities import *


class Backend(Protocol):
    """Interface for a generic backend.

    Attributes:
        name (str): Name of the backend.
        base_url (str): Base URL of the backend.

    Methods:
        list_releases: Retrieve a list of releases.
        get_release_by_tag_name: Retrieve a release by its tag name.
        get_latest_release: Retrieve the latest release.
        get_latest_pre_release: Retrieve the latest pre-release.
        get_release_notes: Retrieve the release notes of a specific release.
        get_contributors: Retrieve the list of contributors.
        get_patches: Retrieve the patches of a specific release.
    """

    name: str
    base_url: str

    def __init__(self, name: str, base_url: str):
        self.name = name
        self.base_url = base_url

    @abstractmethod
    async def list_releases(self, *args: Any, **kwargs: Any) -> list[Release]:
        raise NotImplementedError

    @abstractmethod
    async def get_release_by_tag_name(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    @abstractmethod
    async def get_latest_release(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    @abstractmethod
    async def get_latest_pre_release(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    @abstractmethod
    async def get_contributors(self, *args: Any, **kwargs: Any) -> list[Contributor]:
        raise NotImplementedError

    @abstractmethod
    async def get_patches(self, *args: Any, **kwargs: Any) -> list[dict]:
        raise NotImplementedError


class Repository:
    """A repository that communicates with a specific backend.

    Attributes:
        backend (Backend): The backend instance used to communicate with the repository.
    """

    def __init__(self, backend: Backend):
        self.backend = backend
