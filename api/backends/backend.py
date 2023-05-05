from abc import abstractmethod
from typing import Any
from typing import Protocol

from api.backends.entities import *


class Backend(Protocol):
    name: str
    base_url: str

    def __init__(self, name: str, base_url: str):
        self.name = name
        self.base_url = base_url

    @abstractmethod
    async def list_releases(self, *args: Any, **kwargs: Any) -> list[Release]:
        raise NotImplementedError

    @abstractmethod
    async def get_release(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    @abstractmethod
    async def get_release_by_tag_name(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    async def get_latest_release(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    async def get_latest_pre_release(self, *args: Any, **kwargs: Any) -> Release:
        raise NotImplementedError

    async def get_release_notes(self, *args: Any, **kwargs: Any) -> Notes:
        raise NotImplementedError

    async def get_contributors(self, *args: Any, **kwargs: Any) -> list[Contributor]:
        raise NotImplementedError

    async def get_patches(self, *args: Any, **kwargs: Any) -> dict:
        raise NotImplementedError


class Repository:
    def __init__(self, backend: Backend):
        self.backend = backend
