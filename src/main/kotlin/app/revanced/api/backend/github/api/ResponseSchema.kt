package app.revanced.api.backend.github.api

import kotlinx.serialization.Serializable


class Response {
    interface IUser {
        val login: String
        val avatarUrl: String
        val url: String
    }

    @Serializable
    class User (
        override val login: String,
        override val avatarUrl: String,
        override val url: String,
        val bio: String?,
    ) : IUser

    class Organization {
        @Serializable
        class Member(
            override val login: String,
            override val avatarUrl: String,
            override val url: String,
        ) : IUser

        class Repository {
            @Serializable
            class Contributor(
                override val login: String,
                override val avatarUrl: String,
                override val url: String,
            ) : IUser

            @Serializable
            class Release(
                val tagName: String,
                val assets: Set<Asset>,
                val preReleases: Boolean,
                val createdAt: String,
                val body: String
            ) {
                @Serializable
                class Asset(
                    val browserDownloadUrl: String
                )
            }
        }
    }
}
