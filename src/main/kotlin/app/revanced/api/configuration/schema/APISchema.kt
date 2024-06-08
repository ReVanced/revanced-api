package app.revanced.api.configuration.schema

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class APIRelease(
    val version: String,
    val createdAt: LocalDateTime,
    val changelog: String,
    val assets: Set<APIAsset>,
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
    val gpgKey: APIGpgKey?,
) : APIUser

@Serializable
class APIGpgKey(
    val id: String,
    val url: String,
)

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
    val contributors: Set<APIContributor>,
)

@Serializable
class APIAsset(
    val downloadUrl: String,
) {
    val type = when {
        downloadUrl.endsWith(".jar") -> Type.PATCHES
        downloadUrl.endsWith(".apk") -> Type.INTEGRATIONS
        else -> Type.UNKNOWN
    }

    enum class Type {
        @SerialName("patches")
        PATCHES,

        @SerialName("integrations")
        INTEGRATIONS,

        @SerialName("unknown")
        UNKNOWN,
    }
}

@Serializable
class APIReleaseVersion(
    val version: String,
)

@Serializable
class APIAnnouncement(
    val author: String? = null,
    val title: String,
    val content: String? = null,
    val attachmentUrls: Set<String> = emptySet(),
    val channel: String? = null,
    val archivedAt: LocalDateTime? = null,
    val level: Int = 0,
)

@Serializable
class APIResponseAnnouncement(
    val id: Int,
    val author: String? = null,
    val title: String,
    val content: String? = null,
    val attachmentUrls: Set<String> = emptySet(),
    val channel: String? = null,
    val createdAt: LocalDateTime,
    val archivedAt: LocalDateTime? = null,
    val level: Int = 0,
)

@Serializable
class APIResponseAnnouncementId(
    val id: Int,
)

@Serializable
class APIAnnouncementArchivedAt(
    val archivedAt: LocalDateTime,
)

@Serializable
class APIRateLimit(
    val limit: Int,
    val remaining: Int,
    val reset: LocalDateTime,
)
