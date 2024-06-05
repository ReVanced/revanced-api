package app.revanced.api.repository.backend.github

import app.revanced.api.repository.backend.BackendRepository
import app.revanced.api.repository.backend.BackendRepository.BackendOrganization.BackendMember
import app.revanced.api.repository.backend.BackendRepository.BackendOrganization.BackendRepository.BackendContributor
import app.revanced.api.repository.backend.BackendRepository.BackendOrganization.BackendRepository.BackendRelease
import app.revanced.api.repository.backend.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.BackendAsset
import app.revanced.api.repository.backend.github.api.Request
import app.revanced.api.repository.backend.github.api.Request.Organization.Members
import app.revanced.api.repository.backend.github.api.Request.Organization.Repository.Contributors
import app.revanced.api.repository.backend.github.api.Request.Organization.Repository.Releases
import app.revanced.api.repository.backend.github.api.Response
import app.revanced.api.repository.backend.github.api.Response.GitHubOrganization.GitHubMember
import app.revanced.api.repository.backend.github.api.Response.GitHubOrganization.GitHubRepository.GitHubContributor
import app.revanced.api.repository.backend.github.api.Response.GitHubOrganization.GitHubRepository.GitHubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
                BackendAsset(downloadUrl = it.browserDownloadUrl)
            }.toSet(),
        )
    }

    override suspend fun contributors(
        owner: String,
        repository: String,
    ): Set<BackendContributor> {
        val contributors: Set<GitHubContributor> = client.get(Contributors(owner, repository)).body()

        return contributors.map {
            BackendContributor(
                name = it.login,
                avatarUrl = it.avatarUrl,
                url = it.url,
                contributions = it.contributions,
            )
        }.toSet()
    }

    override suspend fun members(organization: String): Set<BackendMember> {
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