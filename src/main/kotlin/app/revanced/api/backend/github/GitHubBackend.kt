package app.revanced.api.backend.github

import app.revanced.api.backend.Backend
import app.revanced.api.backend.github.api.Request
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import app.revanced.api.backend.github.api.Request.Organization.Repository.Releases
import app.revanced.api.backend.github.api.Request.Organization.Repository.Contributors
import app.revanced.api.backend.github.api.Request.Organization.Members
import app.revanced.api.backend.github.api.Response
import app.revanced.api.backend.github.api.Response.Organization.Repository.Release
import app.revanced.api.backend.github.api.Response.Organization.Repository.Contributor
import app.revanced.api.backend.github.api.Response.Organization.Member
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
class GitHubBackend(token: String? = null) : Backend({
    install(HttpCache)
    install(Resources)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        })
    }

    defaultRequest { url("https://api.github.com") }

    token?.let {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = it,
                        refreshToken = "" // Required dummy value
                    )
                }

                sendWithoutRequest { true }
            }
        }
    }
}) {
    override suspend fun getRelease(
        owner: String,
        repository: String,
        tag: String?,
        preRelease: Boolean
    ): Organization.Repository.Release {
        val release = if (preRelease) {
            val releases: Set<Release> = client.get(Releases(owner, repository)).body()
            releases.firstOrNull { it.preReleases } ?: releases.first() // Latest pre-release or latest release
        } else {
            client.get(
                tag?.let { Releases.Tag(owner, repository, it) }
                    ?: Releases.Latest(owner, repository)
            ).body()
        }

        return Organization.Repository.Release(
            tag = release.tagName,
            releaseNote = release.body,
            createdAt = release.createdAt,
            assets = release.assets.map {
                Organization.Repository.Release.Asset(
                    downloadUrl = it.browserDownloadUrl
                )
            }.toSet()
        )
    }

    override suspend fun getContributors(owner: String, repository: String): Set<Organization.Repository.Contributor> {
        val contributors: Set<Contributor> = client.get(Contributors(owner, repository)).body()

        return contributors.map {
            Organization.Repository.Contributor(
                name = it.login,
                avatarUrl = it.avatarUrl,
                profileUrl = it.url
            )
        }.toSet()
    }

    override suspend fun getMembers(organization: String): Set<Organization.Member> {
        // Get the list of members of the organization.
        val members: Set<Member> = client.get(Members(organization)).body<Set<Member>>()

        return runBlocking(Dispatchers.Default) {
            members.map { member ->
                // Map the member to a user in order to get the bio.
                async {
                    client.get(Request.User(member.login)).body<Response.User>()
                }
            }
        }.awaitAll().map { user ->
            // Map the user back to a member.
            Organization.Member(
                name = user.login,
                avatarUrl = user.avatarUrl,
                profileUrl = user.url,
                bio = user.bio,
                gpgKeysUrl = "https://github.com/${user.login}.gpg",
            )
        }.toSet()
    }
}
