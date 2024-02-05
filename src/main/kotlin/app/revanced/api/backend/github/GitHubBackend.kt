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
import app.revanced.api.backend.Backend.BackendOrganization.BackendMember
import app.revanced.api.backend.Backend.BackendOrganization.BackendRepository.BackendRelease
import app.revanced.api.backend.Backend.BackendOrganization.BackendRepository.BackendContributor
import app.revanced.api.backend.Backend.BackendOrganization.BackendRepository.BackendRelease.BackendAsset
import app.revanced.api.backend.github.api.Request.Organization.Repository.Releases
import app.revanced.api.backend.github.api.Request.Organization.Repository.Contributors
import app.revanced.api.backend.github.api.Request.Organization.Members
import app.revanced.api.backend.github.api.Response
import app.revanced.api.backend.github.api.Response.GitHubOrganization.GitHubRepository.GitHubRelease
import app.revanced.api.backend.github.api.Response.GitHubOrganization.GitHubRepository.GitHubContributor
import app.revanced.api.backend.github.api.Response.GitHubOrganization.GitHubMember
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.dsl.bind
import org.koin.dsl.module

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
    ): BackendRelease {
        val release: GitHubRelease = if (tag != null)
            client.get(Releases.Tag(owner, repository, tag)).body()
        else
            client.get(Releases.Latest(owner, repository)).body()


        return BackendRelease(
            tag = release.tagName,
            releaseNote = release.body,
            createdAt = release.createdAt.toLocalDateTime(TimeZone.UTC),
            assets = release.assets.map {
                BackendAsset(downloadUrl = it.browserDownloadUrl)
            }.toSet()
        )
    }

    override suspend fun getContributors(
        owner: String,
        repository: String
    ): Set<BackendContributor> {
        val contributors: Set<GitHubContributor> = client.get(Contributors(owner, repository)).body()

        return contributors.map {
            BackendContributor(
                name = it.login,
                avatarUrl = it.avatarUrl,
                url = it.url,
                contributions = it.contributions
            )
        }.toSet()
    }

    override suspend fun getMembers(organization: String): Set<BackendMember> {
        // Get the list of members of the organization.
        val members: Set<GitHubMember> = client.get(Members(organization)).body()

        return runBlocking(Dispatchers.Default) {
            members.map { member ->
                // Map the member to a user in order to get the bio.
                async {
                    client.get(Request.User(member.login)).body<Response.GitHubUser>()
                }
            }
        }.awaitAll().map { user ->
            // Map the user back to a member.
            BackendMember(
                name = user.login,
                avatarUrl = user.avatarUrl,
                url = user.url,
                bio = user.bio,
                gpgKeysUrl = "https://github.com/${user.login}.gpg",
            )
        }.toSet()
    }
}