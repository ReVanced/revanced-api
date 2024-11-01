package app.revanced.api.configuration.schema

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

interface ApiUser {
    val name: String
    val avatarUrl: String
    val url: String
}

@Serializable
class ApiMember(
    override val name: String,
    override val avatarUrl: String,
    override val url: String,
    val bio: String?,
    val gpgKey: ApiGpgKey?,
) : ApiUser

@Serializable
class ApiGpgKey(
    val id: String,
    val url: String,
)

@Serializable
class ApiContributor(
    override val name: String,
    override val avatarUrl: String,
    override val url: String,
    val contributions: Int,
) : ApiUser

@Serializable
class APIContributable(
    val name: String,
    val url: String,
    // Using a list instead of a set because set semantics are unnecessary here.
    val contributors: List<ApiContributor>,
)

@Serializable
class ApiRelease<T>(
    val version: String,
    val createdAt: LocalDateTime,
    val description: String,
    // Using a list instead of a set because set semantics are unnecessary here.
    val assets: List<T>,
)

@Serializable
class ApiManagerAsset(
    val downloadUrl: String,
)

@Serializable
class ApiPatchesAsset(
    val downloadUrl: String,
    val signatureDownloadUrl: String,
    // TODO: Remove this eventually when integrations are merged into patches.
    val name: ApiAssetName,
)

@Serializable
enum class ApiAssetName {
    PATCHES,
    INTEGRATION,
}

@Serializable
class ApiReleaseVersion(
    val version: String,
)

@Serializable
class ApiAnnouncement(
    val author: String? = null,
    val title: String,
    val content: String? = null,
    // Using a list instead of a set because set semantics are unnecessary here.
    val attachments: List<String> = emptyList(),
    // Using a list instead of a set because set semantics are unnecessary here.
    val tags: List<String> = emptyList(),
    val archivedAt: LocalDateTime? = null,
    val level: Int = 0,
)

@Serializable
class ApiResponseAnnouncement(
    val id: Int,
    val author: String? = null,
    val title: String,
    val content: String? = null,
    // Using a list instead of a set because set semantics are unnecessary here.
    val attachments: List<String> = emptyList(),
    // Using a list instead of a set because set semantics are unnecessary here.
    val tags: List<Int> = emptyList(),
    val createdAt: LocalDateTime,
    val archivedAt: LocalDateTime? = null,
    val level: Int = 0,
)

@Serializable
class ApiResponseAnnouncementId(
    val id: Int,
)

@Serializable
class ApiAnnouncementArchivedAt(
    val archivedAt: LocalDateTime,
)

@Serializable
class ApiAnnouncementTag(
    val id: Int,
    val name: String,
)

@Serializable
class ApiRateLimit(
    val limit: Int,
    val remaining: Int,
    val reset: LocalDateTime,
)

@Serializable
class ApiAssetPublicKeys(
    val patchesPublicKey: String,
    val integrationsPublicKey: String,
)

@Serializable
class APIAbout(
    val name: String,
    val about: String,
    val keys: String,
    val branding: Branding?,
    val contact: Contact?,
    // Using a list instead of a set because set semantics are unnecessary here.
    val socials: List<Social>?,
    val donations: Donations?,
) {
    @Serializable
    class Branding(
        val logo: String,
    )

    @Serializable
    class Contact(
        val email: String,
    )

    @Serializable
    class Social(
        val name: String,
        val url: String,
        val preferred: Boolean? = false,
    )

    @Serializable
    class Wallet(
        val network: String,
        val currencyCode: String,
        val address: String,
        val preferred: Boolean? = false,
    )

    @Serializable
    class Link(
        val name: String,
        val url: String,
        val preferred: Boolean? = false,
    )

    @Serializable
    class Donations(
        // Using a list instead of a set because set semantics are unnecessary here.
        val wallets: List<Wallet>?,
        // Using a list instead of a set because set semantics are unnecessary here.
        val links: List<Link>?,
    )
}

@Serializable
class ApiToken(val token: String)
