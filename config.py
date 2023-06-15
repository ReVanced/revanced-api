# Social Links

social_links: dict[str, str] = {
    "website": "https://revanced.app",
    "github": "https://github.com/revanced",
    "twitter": "https://twitter.com/revancedapp",
    "discord": "https://revanced.app/discord",
    "reddit": "https://www.reddit.com/r/revancedapp",
    "telegram": "https://t.me/app_revanced",
    "youtube": "https://www.youtube.com/@ReVanced",
}

# Donation info

donation_info: dict[str, str] = {
    "liberapay": "https://liberapay.com/ReVanced/donate",
    "github": "https://github.com/sponsors/ReVanced",
    "btc": "bc1q4x8j6mt27y5gv0q625t8wkr87ruy8fprpy4v3f",
    "doge": "D8GH73rNjudgi6bS2krrXWEsU9KShedLXp",
    "eth": "0x7ab4091e00363654bf84B34151225742cd92FCE5",
    "ltc": "LbJi8EuoDcwaZvykcKmcrM74jpjde23qJ2",
    "xmr": "46YwWDbZD6jVptuk5mLHsuAmh1BnUMSjSNYacozQQEraWSQ93nb2yYVRHoMR6PmFYWEHsLHg9tr1cH5M8Rtn7YaaGQPCjSh",
}

# API Configuration

backend: str = "github"
redis: dict[str, str | int] = {"host": "localhost", "port": 6379}

# GitHub Backend Configuration

owner: str = "revanced"

# API Versioning

api_version: str = "v2"
openapi_version: str = "2.0.0"
openapi_title: str = "ReVanced API"
openapi_description: str = """
## The official JSON API for ReVanced Releases 🚀

### Links

- [Changelogs](https://github.com/revanced/)
- [Official links to ReVanced](https://revanced.app)

### Important Information

* Rate Limiting - 60 requests per minute
* Cache - 5 minutes

### Additional Notes

1. Breaking changes are to be expected
2. Client side caching is advised to avoid unnecessary requests
3. Abuse of the API will result in IP blocks
"""

# Testing Configuration

github_testing_repository: str = "revanced-patches"
github_testing_tag: str = "v2.173.0"
apkdl_testing_package: str = "com.google.android.youtube"

# Old API Configuration

compat_api_version: str = "v1"
compat_repositories: set = (
    "revanced-patcher",
    "revanced-patches",
    "revanced-integrations",
    "revanced-manager",
    "revanced-cli",
    "revanced-website",
    "revanced-releases-api",
)
