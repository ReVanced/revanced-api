package app.revanced.api.backend.github.api

import io.ktor.resources.*

class Request {
    @Resource("/users/{username}")
    class User(val username: String)

    class Organization {
        @Resource("/orgs/{org}/members")
        class Members(val org: String)

        class Repository {
            @Resource("/repos/{owner}/{repo}/contributors")
            class Contributors(val owner: String, val repo: String)

            @Resource("/repos/{owner}/{repo}/releases")
            class Releases(val owner: String, val repo: String) {
                @Resource("/repos/{owner}/{repo}/releases/tags/{tag}")
                class Tag(val owner: String, val repo: String, val tag: String)

                @Resource("/repos/{owner}/{repo}/releases/latest")
                class Latest(val owner: String, val repo: String)
            }
        }
    }
}
