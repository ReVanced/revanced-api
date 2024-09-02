package app.revanced.api.configuration.repository

import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendMember
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendContributor
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.BackendAsset
import app.revanced.api.configuration.repository.GitHubOrganization.GitHubRepository.GitHubContributor
import app.revanced.api.configuration.repository.GitHubOrganization.GitHubRepository.GitHubRelease
import app.revanced.api.configuration.repository.Organization.Repository.Contributors
import app.revanced.api.configuration.repository.Organization.Repository.Releases
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.resources.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GitHubBackendRepository(client: HttpClient) : BackendRepository(client) {
    override suspend fun release(
        owner: String,
        repository: String,
        tag: String?,
    ): BackendRelease {
        val release: GitHubRelease = if (tag != null) {
            client.get(Releases.Tag(owner, repository, tag)).body()
        } else {
            client.get(Releases.Latest(owner, repository)).body()
        }

        return BackendRelease(
            tag = release.tagName,
            releaseNote = release.body,
            createdAt = release.createdAt.toLocalDateTime(TimeZone.UTC),
            assets = release.assets.map {
                BackendAsset(
                    name = it.name,
                    downloadUrl = it.browserDownloadUrl,
                )
            },
        )
    }

    override suspend fun contributors(
        owner: String,
        repository: String,
    ): List<BackendContributor> {
        val contributors: List<GitHubContributor> = client.get(
            Contributors(
                owner,
                repository,
            ),
        ).body()

        return contributors.map {
            BackendContributor(
                name = it.login,
                avatarUrl = it.avatarUrl,
                url = it.htmlUrl,
                contributions = it.contributions,
            )
        }
    }

    override suspend fun members(organization: String): List<BackendMember> {
        // Get the list of members of the organization.
        val publicMembers: List<GitHubOrganization.GitHubMember> = client.get(Organization.PublicMembers(organization)).body()

        return coroutineScope {
            publicMembers.map { member ->
                async {
                    awaitAll(
                        async {
                            // Get the user.
                            client.get(User(member.login)).body<GitHubUser>()
                        },
                        async {
                            // Get the GPG key of the user.
                            client.get(User.GpgKeys(member.login)).body<List<GitHubUser.GitHubGpgKey>>()
                        },
                    )
                }
            }
        }.awaitAll().map { responses ->
            val user = responses[0] as GitHubUser

            @Suppress("UNCHECKED_CAST")
            val gpgKeys = responses[1] as List<GitHubUser.GitHubGpgKey>

            BackendMember(
                name = user.login,
                avatarUrl = user.avatarUrl,
                url = user.htmlUrl,
                bio = user.bio,
                gpgKeys =
                BackendMember.GpgKeys(
                    ids = gpgKeys.map { it.keyId },
                    url = "https://api.github.com/users/${user.login}/gpg_keys",
                ),
            )
        }
    }

    override suspend fun rateLimit(): BackendRateLimit {
        val rateLimit: GitHubRateLimit = client.get(RateLimit()).body()

        return BackendRateLimit(
            limit = rateLimit.rate.limit,
            remaining = rateLimit.rate.remaining,
            reset = Instant.fromEpochSeconds(rateLimit.rate.reset).toLocalDateTime(TimeZone.UTC),
        )
    }
}

interface IGitHubUser {
    val login: String
    val avatarUrl: String
    val htmlUrl: String
}

@Serializable
class GitHubUser(
    override val login: String,
    override val avatarUrl: String,
    override val htmlUrl: String,
    val bio: String?,
) : IGitHubUser {
    @Serializable
    class GitHubGpgKey(
        val keyId: String,
    )
}

class GitHubOrganization {
    @Serializable
    class GitHubMember(
        override val login: String,
        override val avatarUrl: String,
        override val htmlUrl: String,
    ) : IGitHubUser

    class GitHubRepository {
        @Serializable
        class GitHubContributor(
            override val login: String,
            override val avatarUrl: String,
            override val htmlUrl: String,
            val contributions: Int,
        ) : IGitHubUser

        @Serializable
        class GitHubRelease(
            val tagName: String,
            // Using a list instead of a set because set semantics are unnecessary here.
            val assets: List<GitHubAsset>,
            val createdAt: Instant,
            val body: String,
        ) {
            @Serializable
            class GitHubAsset(
                val name: String,
                val browserDownloadUrl: String,
            )
        }
    }
}

@Serializable
class GitHubRateLimit(
    val rate: Rate,
) {
    @Serializable
    class Rate(
        val limit: Int,
        val remaining: Int,
        val reset: Long,
    )
}

@Resource("/users/{login}")
class User(val login: String) {
    @Resource("/users/{login}/gpg_keys")
    class GpgKeys(val login: String)
}

class Organization {
    @Resource("/orgs/{org}/public_members")
    class PublicMembers(val org: String)

    class Repository {
        @Resource("/repos/{owner}/{repo}/contributors")
        class Contributors(val owner: String, val repo: String, @SerialName("per_page") val perPage: Int = 100)

        class Releases {
            @Resource("/repos/{owner}/{repo}/releases/tags/{tag}")
            class Tag(val owner: String, val repo: String, val tag: String)

            @Resource("/repos/{owner}/{repo}/releases/latest")
            class Latest(val owner: String, val repo: String)
        }
    }
}

@Resource("/rate_limit")
class RateLimit
