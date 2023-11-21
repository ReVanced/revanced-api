from cytoolz import keyfilter
from config import api_versions


def get_version(value: str) -> str:
    result = keyfilter(lambda key: value in api_versions[key], api_versions)

    return list(result.keys())[0] if result else "v0"
