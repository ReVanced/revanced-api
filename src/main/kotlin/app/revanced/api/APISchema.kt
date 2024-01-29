package app.revanced.api

import kotlinx.serialization.Serializable

@Serializable
class APIRelease(
    val version: String,
    val createdAt: String,
    val changelog: String,
    val assets: Set<APIAsset>
)

interface APIUser {
    val name: String
    val avatarUrl: String
    val url: String
}

@Serializable
class APIMember(
    override val name: String,
    override val avatarUrl: String,
    override val url: String,
    val gpgKeysUrl: String
) : APIUser

@Serializable
class APIContributor(
    override val name: String,
    override val avatarUrl: String,
    override val url: String,
    val contributions: Int,
) : APIUser

@Serializable
class APIContributable(
    val name: String,
    val contributors: Set<APIContributor>
)

@Serializable
class APIAsset(
    val downloadUrl: String,
) {
    val type = when {
        downloadUrl.endsWith(".jar") -> "patches"
        downloadUrl.endsWith(".apk") -> "integrations"
        else -> "unknown"
    }
}

@Serializable
class APIReleaseVersion(
    val version: String
)