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

# API Configuration

backend: str = "github"
redis: dict[str, str | int] = {"host": "localhost", "port": 6379}

# GitHub Backend Configuration

owner: str = "revanced"
