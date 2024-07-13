package app.revanced.api.configuration.schema

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

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
    val bio: String?,
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
    // Using a list instead of a set because set semantics are unnecessary here.
    val contributors: List<APIContributor>,
)

@Serializable
class APIRelease<T>(
    val version: String,
    val createdAt: LocalDateTime,
    val description: String,
    // Using a list instead of a set because set semantics are unnecessary here.
    val assets: List<T>,
)

@Serializable
class APIManagerAsset(
    val downloadUrl: String,
)

@Serializable
class APIPatchesAsset(
    val downloadUrl: String,
    val signatureDownloadUrl: String,
    // TODO: Remove this eventually when integrations are merged into patches.
    val name: APIAssetName,
)

@Serializable
enum class APIAssetName {
    PATCHES,
    INTEGRATION,
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
    // Using a list instead of a set because set semantics are unnecessary here.
    val attachmentUrls: List<String> = emptyList(),
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
    // Using a list instead of a set because set semantics are unnecessary here.
    val attachmentUrls: List<String> = emptyList(),
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

@Serializable
class APIAssetPublicKeys(
    val patchesPublicKey: String,
    val integrationsPublicKey: String,
)
