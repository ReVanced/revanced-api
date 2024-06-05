package app.revanced.api.configuration.repository.backend

import io.ktor.client.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 *  The backend of the application used to get data for the API.
 *
 *  @param client The HTTP client to use for requests.
 */
abstract class BackendRepository internal constructor(
    protected val client: HttpClient,
) {
    /**
     * A user.
     *
     * @property name The name of the user.
     * @property avatarUrl The URL to the avatar of the user.
     * @property url The URL to the profile of the user.
     */
    interface BackendUser {
        val name: String
        val avatarUrl: String
        val url: String
    }

    /**
     * An organization.
     *
     * @property members The members of the organization.
     */
    class BackendOrganization(
        val members: Set<BackendMember>,
    ) {
        /**
         * A member of an organization.
         *
         * @property name The name of the member.
         * @property avatarUrl The URL to the avatar of the member.
         * @property url The URL to the profile of the member.
         * @property bio The bio of the member.
         * @property gpgKeysUrl The URL to the GPG keys of the member.
         */
        @Serializable
        class BackendMember(
            override val name: String,
            override val avatarUrl: String,
            override val url: String,
            val bio: String?,
            val gpgKeysUrl: String,
        ) : BackendUser

        /**
         * A repository of an organization.
         *
         * @property contributors The contributors of the repository.
         */
        class BackendRepository(
            val contributors: Set<BackendContributor>,
        ) {
            /**
             * A contributor of a repository.
             *
             * @property name The name of the contributor.
             * @property avatarUrl The URL to the avatar of the contributor.
             * @property url The URL to the profile of the contributor.
             * @property contributions The number of contributions of the contributor.
             */
            @Serializable
            class BackendContributor(
                override val name: String,
                override val avatarUrl: String,
                override val url: String,
                val contributions: Int,
            ) : BackendUser

            /**
             * A release of a repository.
             *
             * @property tag The tag of the release.
             * @property assets The assets of the release.
             * @property createdAt The date and time the release was created.
             * @property releaseNote The release note of the release.
             */
            @Serializable
            class BackendRelease(
                val tag: String,
                val releaseNote: String,
                val createdAt: LocalDateTime,
                val assets: Set<BackendAsset>,
            ) {
                /**
                 * An asset of a release.
                 *
                 * @property downloadUrl The URL to download the asset.
                 */
                @Serializable
                class BackendAsset(
                    val downloadUrl: String,
                )
            }
        }
    }

    /**
     * Get a release of a repository.
     *
     * @param owner The owner of the repository.
     * @param repository The name of the repository.
     * @param tag The tag of the release. If null, the latest release is returned.
     * @return The release.
     */
    abstract suspend fun release(
        owner: String,
        repository: String,
        tag: String? = null,
    ): BackendOrganization.BackendRepository.BackendRelease

    /**
     * Get the contributors of a repository.
     *
     * @param owner The owner of the repository.
     * @param repository The name of the repository.
     * @return The contributors.
     */
    abstract suspend fun contributors(owner: String, repository: String): Set<BackendOrganization.BackendRepository.BackendContributor>

    /**
     * Get the members of an organization.
     *
     * @param organization The name of the organization.
     * @return The members.
     */
    abstract suspend fun members(organization: String): Set<BackendOrganization.BackendMember>
}
