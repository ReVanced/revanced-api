package app.revanced.api.backend

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.serialization.Serializable

/**
 *  The backend of the application used to get data for the API.
 *
 *  @param httpClientConfig The configuration of the HTTP client.
 */
abstract class Backend(
    httpClientConfig: HttpClientConfig<OkHttpConfig>.() -> Unit = {}
) {
    protected val client: HttpClient = HttpClient(OkHttp, httpClientConfig)

    /**
     * A user.
     *
     * @property name The name of the user.
     * @property avatarUrl The URL to the avatar of the user.
     * @property profileUrl The URL to the profile of the user.
     */
    interface User {
        val name: String
        val avatarUrl: String
        val profileUrl: String
    }

    /**
     * An organization.
     *
     * @property members The members of the organization.
     */
    class Organization(
        val members: Set<Member>
    ) {
        /**
         * A member of an organization.
         *
         * @property name The name of the member.
         * @property avatarUrl The URL to the avatar of the member.
         * @property profileUrl The URL to the profile of the member.
         * @property bio The bio of the member.
         * @property gpgKeysUrl The URL to the GPG keys of the member.
         */
        @Serializable
        class Member (
            override val name: String,
            override val avatarUrl: String,
            override val profileUrl: String,
            val bio: String?,
            val gpgKeysUrl: String?
        ) : User

        /**
         * A repository of an organization.
         *
         * @property contributors The contributors of the repository.
         */
        class Repository(
            val contributors: Set<Contributor>
        ) {
            /**
             * A contributor of a repository.
             *
             * @property name The name of the contributor.
             * @property avatarUrl The URL to the avatar of the contributor.
             * @property profileUrl The URL to the profile of the contributor.
             */
            @Serializable
            class Contributor(
                override val name: String,
                override val avatarUrl: String,
                override val profileUrl: String
            ) : User

            /**
             * A release of a repository.
             *
             * @property tag The tag of the release.
             * @property assets The assets of the release.
             * @property createdAt The date and time the release was created.
             * @property releaseNote The release note of the release.
             */
            @Serializable
            class Release(
                val tag: String,
                val releaseNote: String,
                val createdAt: String,
                val assets: Set<Asset>
            ) {
                /**
                 * An asset of a release.
                 *
                 * @property downloadUrl The URL to download the asset.
                 */
                @Serializable
                class Asset(
                    val downloadUrl: String
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
     * @param preRelease Whether to return a pre-release.
     * If no pre-release exists, the latest release is returned.
     * If tag is not null, this parameter is ignored.
     * @return The release.
     */
    abstract suspend fun getRelease(
        owner: String,
        repository: String,
        tag: String? = null,
        preRelease: Boolean = false
    ): Organization.Repository.Release

    /**
     * Get the contributors of a repository.
     *
     * @param owner The owner of the repository.
     * @param repository The name of the repository.
     * @return The contributors.
     */
    abstract suspend fun getContributors(owner: String, repository: String): Set<Organization.Repository.Contributor>

    /**
     * Get the members of an organization.
     *
     * @param organization The name of the organization.
     * @return The members.
     */
    abstract suspend fun getMembers(organization: String): Set<Organization.Member>
}
