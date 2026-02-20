package app.revanced.api.server.routes

import app.revanced.api.server.*
import app.revanced.api.server.installCache
import app.revanced.api.server.installNoCache
import app.revanced.api.server.repository.ConfigurationRepository
import app.revanced.api.server.respondOrNotFound
import app.revanced.api.server.services.ApiService
import app.revanced.api.server.services.AuthenticationService
import io.ktor.http.*
import io.ktor.openapi.ExampleObject
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet


internal fun Route.apiRoute() {
    val apiService = koinGet<ApiService>()
    val configuration = koinGet<ConfigurationRepository>()
    val authenticationService = koinGet<AuthenticationService>()

    rateLimit(RateLimitName("strong")) {
        authenticate("auth-digest") {
            route("token") {
                get {
                    call.respond(authenticationService.newToken())
                }.describe {
                    description = "Get a new authorization token"
                    summary = "Get authorization token"

                    parameters {
                        header("Authorization") {
                            required = true
                            example(
                                "Digest access authentication", ExampleObject(
                                    "Digest " +
                                            "username=\"ReVanced\", " +
                                            "realm=\"ReVanced\", " +
                                            "nonce=\"abc123\", " +
                                            "uri=\"/${configuration.apiVersion}/token\", " +
                                            "algorithm=SHA-256, " +
                                            "response=\"yxz456\"",
                                )
                            )
                        }
                    }
                    responses {
                        HttpStatusCode.OK {
                            description = "The authorization token"
                            schema = jsonSchema<ApiToken>()
                            ContentType.Application.Json()
                        }
                        HttpStatusCode.Unauthorized()
                    }
                }
            }.describe {
                tag("API")
            }
        }

        route("contributors") {
            installCache(1.days)

            get {
                call.respond(apiService.contributors())
            }.describe {
                description = "Get the list of contributors"
                summary = "Get contributors"

                responses {
                    HttpStatusCode.OK {
                        description = "The list of contributors"
                        schema = jsonSchema<Set<APIContributable>>()
                        ContentType.Application.Json()
                    }
                }
            }
        }.describe {
            tag("API")
        }

        route("team") {
            installCache(1.days)

            get {
                call.respond(apiService.team())
            }.describe {
                description = "Get the list of team members"
                summary = "Get team members"

                responses {
                    HttpStatusCode.OK {
                        description = "The list of team members"
                        schema = jsonSchema<Set<ApiMember>>()
                        ContentType.Application.Json()
                    }
                }
            }
        }.describe {
            tag("API")
        }
    }

    route("about") {
        installCache(1.days)


        get {
            call.respond(apiService.about)
        }.describe {
            description = "Get information about the API"
            summary = "Get about"

            responses {
                HttpStatusCode.OK {
                    description = "Information about the API"
                    schema = jsonSchema<APIAbout>()
                    ContentType.Application.Json()
                }
            }
        }
    }.describe {
        tag("API")
    }

    route("ping") {
        installNoCache()

        handle {
            call.respond(HttpStatusCode.NoContent)
        }
    }.describe {
        tag("API")

        description = "Ping the server to check if it's reachable"
        summary = "Ping"

        responses {
            HttpStatusCode.NoContent {
                description = "The server is reachable"
            }
        }
    }

    rateLimit(RateLimitName("weak")) {
        route("backend/rate_limit") {
            get {
                call.respondOrNotFound(apiService.rateLimit())
            }.describe {
                description = "Get the rate limit of the backend"
                summary = "Get rate limit of backend"

                responses {
                    HttpStatusCode.OK {
                        description = "The rate limit of the backend"
                        schema = jsonSchema<ApiRateLimit>()
                        ContentType.Application.Json()
                    }
                }
            }
        }.describe {
            tag("API")
        }

        staticFiles("/", apiService.versionedStaticFilesPath)
    }
}
