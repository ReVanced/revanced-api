# API Configuration

backend: str = "github"
redis: dict[str, str | int] = {"host": "localhost", "port": 6379}

# GitHub Backend Configuration

owner: str = "revanced"
default_repository: str = ".github"

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
compat_repositories: list = [
    "revanced-patcher",
    "revanced-patches",
    "revanced-integrations",
    "revanced-manager",
    "revanced-cli",
    "revanced-website",
    "revanced-api",
    "revanced-releases-api",
]

# Branding

branding_links: dict[str, str] = {
    {"assettype": "logo", "url": "https://github.com/ReVanced/revanced-branding/raw/main/assets/revanced-logo/revanced-logo.svg"},
    {"assettype": "horizontal-light", "url": "https://github.com/ReVanced/revanced-branding/raw/main/assets/revanced-headline/revanced-headline-horizontal-light.svg"},
    {"assettype": "horizontal-dark", "url": "https://github.com/ReVanced/revanced-branding/raw/main/assets/revanced-headline/revanced-headline-horizontal-dark.svg"},
    {"assettype": "vertical-light", "url": "https://github.com/ReVanced/revanced-branding/raw/main/assets/revanced-headline/revanced-headline-vertical-light.svg"},
    {"assettype": "vertical-dark", "url": "https://github.com/ReVanced/revanced-branding/raw/main/assets/revanced-headline/revanced-headline-vertical-dark.svg"},
}

# Social Links

social_links: list[dict[str, str | bool]] = [
    {"name": "Website", "url": "https://revanced.app", "preferred": True},
    {"name": "GitHub", "url": "https://github.com/revanced", "preferred": False},
    {"name": "Twitter", "url": "https://twitter.com/revancedapp", "preferred": False},
    {"name": "Discord", "url": "https://revanced.app/discord", "preferred": True},
    {
        "name": "Reddit",
        "url": "https://www.reddit.com/r/revancedapp",
        "preferred": False,
    },
    {"name": "Telegram", "url": "https://t.me/app_revanced", "preferred": False},
    {"name": "YouTube", "url": "https://www.youtube.com/@ReVanced", "preferred": False},
]

# Donation info

wallets: list[dict[str, str | bool]] = [
    {
        "network": "Bitcoin",
        "currency_code": "BTC",
        "address": "bc1q4x8j6mt27y5gv0q625t8wkr87ruy8fprpy4v3f",
        "preferred": False,
    },
    {
        "network": "Dogecoin",
        "currency_code": "DOGE",
        "address": "D8GH73rNjudgi6bS2krrXWEsU9KShedLXp",
        "preferred": True,
    },
    {
        "network": "Ethereum",
        "currency_code": "ETH",
        "address": "0x7ab4091e00363654bf84B34151225742cd92FCE5",
        "preferred": False,
    },
    {
        "network": "Litecoin",
        "currency_code": "LTC",
        "address": "LbJi8EuoDcwaZvykcKmcrM74jpjde23qJ2",
        "preferred": False,
    },
    {
        "network": "Monero",
        "currency_code": "XMR",
        "address": "46YwWDbZD6jVptuk5mLHsuAmh1BnUMSjSNYacozQQEraWSQ93nb2yYVRHoMR6PmFYWEHsLHg9tr1cH5M8Rtn7YaaGQPCjSh",
        "preferred": False,
    },
]

links: list[dict[str, str | bool]] = [
    {
        "name": "Open Collective",
        "url": "https://opencollective.com/revanced",
        "preferred": True,
    },
    {
        "name": "GitHub Sponsors",
        "url": "https://github.com/sponsors/ReVanced",
        "preferred": False,
    },
]

default_info: dict[str, str | list[str | bool] | bool] = {
    "name": "ReVanced",
    "about": "ReVanced was born out of Vanced's discontinuation and it is our goal to continue the legacy of what Vanced left behind. Thanks to ReVanced Patcher, it's possible to create long-lasting patches for nearly any Android app. ReVanced's patching system is designed to allow patches to work on new versions of the apps automatically with bare minimum maintenance.",
    "contact": {"email": "contact@revanced.app"},
    "socials": social_links,
    "donations": {"wallets": wallets, "links": links},
}
