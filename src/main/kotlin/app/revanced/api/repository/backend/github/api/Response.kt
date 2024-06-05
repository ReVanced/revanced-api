package app.revanced.api.repository.backend.github.api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

class Response {
    interface IGitHubUser {
        val login: String
        val avatarUrl: String
        val url: String
    }

    @Serializable
    class GitHubUser(
        override val login: String,
        override val avatarUrl: String,
        override val url: String,
        val bio: String?,
    ) : IGitHubUser

    class GitHubOrganization {
        @Serializable
        class GitHubMember(
            override val login: String,
            override val avatarUrl: String,
            override val url: String,
        ) : IGitHubUser

        class GitHubRepository {
            @Serializable
            class GitHubContributor(
                override val login: String,
                override val avatarUrl: String,
                override val url: String,
                val contributions: Int,
            ) : IGitHubUser

            @Serializable
            class GitHubRelease(
                val tagName: String,
                val assets: Set<GitHubAsset>,
                val createdAt: Instant,
                val body: String,
            ) {
                @Serializable
                class GitHubAsset(
                    val browserDownloadUrl: String,
                )
            }
        }
    }
}
