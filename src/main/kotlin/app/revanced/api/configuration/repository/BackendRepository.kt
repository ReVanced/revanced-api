package app.revanced.api.configuration.repository

import io.ktor.client.*
import kotlinx.datetime.LocalDateTime

/**
 *  The backend of the API used to get data.
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
        // Using a list instead of a set because set semantics are unnecessary here.
        val members: List<BackendMember>,
    ) {
        /**
         * A member of an organization.
         *
         * @property name The name of the member.
         * @property avatarUrl The URL to the avatar of the member.
         * @property url The URL to the profile of the member.
         * @property bio The bio of the member.
         * @property gpgKeys The GPG key of the member.
         */
        class BackendMember(
            override val name: String,
            override val avatarUrl: String,
            override val url: String,
            val bio: String?,
            val gpgKeys: GpgKeys,
        ) : BackendUser {
            /**
             * The GPG keys of a member.
             *
             * @property ids The IDs of the GPG keys.
             * @property url The URL to the GPG master key.
             */
            class GpgKeys(
                // Using a list instead of a set because set semantics are unnecessary here.
                val ids: List<String>,
                val url: String,
            )
        }

        /**
         * A repository of an organization.
         *
         * @property contributors The contributors of the repository.
         */
        class BackendRepository(
            // Using a list instead of a set because set semantics are unnecessary here.
            val contributors: List<BackendContributor>,
        ) {
            /**
             * A contributor of a repository.
             *
             * @property name The name of the contributor.
             * @property avatarUrl The URL to the avatar of the contributor.
             * @property url The URL to the profile of the contributor.
             * @property contributions The number of contributions of the contributor.
             */
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
            class BackendRelease(
                val tag: String,
                val releaseNote: String,
                val createdAt: LocalDateTime,
                // Using a list instead of a set because set semantics are unnecessary here.
                val assets: List<BackendAsset>,
            ) {
                companion object {
                    fun List<BackendAsset>.first(assetRegex: Regex) = first { assetRegex.containsMatchIn(it.name) }
                }

                /**
                 * An asset of a release.
                 *
                 * @property name The name of the asset.
                 * @property downloadUrl The URL to download the asset.
                 */
                class BackendAsset(
                    val name: String,
                    val downloadUrl: String,
                )
            }
        }
    }

    /**
     * The rate limit of the backend.
     *
     * @property limit The limit of the rate limit.
     * @property remaining The remaining requests of the rate limit.
     * @property reset The date and time the rate limit resets.
     */
    class BackendRateLimit(
        val limit: Int,
        val remaining: Int,
        val reset: LocalDateTime,
    )

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
    abstract suspend fun contributors(owner: String, repository: String): List<BackendOrganization.BackendRepository.BackendContributor>

    /**
     * Get the members of an organization.
     *
     * @param organization The name of the organization.
     * @return The members.
     */
    abstract suspend fun members(organization: String): List<BackendOrganization.BackendMember>

    /**
     * Get the rate limit of the backend.
     *
     * @return The rate limit.
     */
    abstract suspend fun rateLimit(): BackendRateLimit?
}
