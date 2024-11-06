package app.revanced.api.configuration.routes

import app.revanced.api.configuration.*
import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNoCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.services.ApiService
import app.revanced.api.configuration.services.AuthenticationService
import io.bkbn.kompendium.core.metadata.*
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet

internal fun Route.apiRoute() {
    val apiService = koinGet<ApiService>()
    val authenticationService = koinGet<AuthenticationService>()

    rateLimit(RateLimitName("strong")) {
        authenticate("auth-digest") {
            route("token") {
                installTokenRouteDocumentation()

                get {
                    call.respond(authenticationService.newToken())
                }
            }
        }

        route("contributors") {
            installCache(1.days)

            installContributorsRouteDocumentation()

            get {
                call.respond(apiService.contributors())
            }
        }

        route("team") {
            installCache(1.days)

            installTeamRouteDocumentation()

            get {
                call.respond(apiService.team())
            }
        }
    }

    route("about") {
        installCache(1.days)

        installAboutRouteDocumentation()

        get {
            call.respond(apiService.about)
        }
    }

    route("ping") {
        installNoCache()

        installPingRouteDocumentation()

        handle {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    rateLimit(RateLimitName("weak")) {
        route("backend/rate_limit") {
            installRateLimitRouteDocumentation()

            get {
                call.respondOrNotFound(apiService.rateLimit())
            }
        }

        staticFiles("/", apiService.versionedStaticFilesPath)
    }
}

private fun Route.installAboutRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get information about the API")
        summary("Get about")
        response {
            description("Information about the API")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIAbout>()
        }
    }
}

private fun Route.installRateLimitRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get the rate limit of the backend")
        summary("Get rate limit of backend")
        response {
            description("The rate limit of the backend")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiRateLimit>()
        }
    }
}

private fun Route.installPingRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    head = HeadInfo.builder {
        description("Ping the server")
        summary("Ping")
        response {
            description("The server is reachable")
            responseCode(HttpStatusCode.NoContent)
            responseType<Unit>()
        }
    }
}

private fun Route.installTeamRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get the list of team members")
        summary("Get team members")
        response {
            description("The list of team members")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<Set<ApiMember>>()
        }
    }
}

private fun Route.installContributorsRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get the list of contributors")
        summary("Get contributors")
        response {
            description("The list of contributors")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<Set<APIContributable>>()
        }
    }
}

private fun Route.installTokenRouteDocumentation() = installNotarizedRoute {
    val configuration = koinGet<ConfigurationRepository>()

    tags = setOf("API")

    get = GetInfo.builder {
        description("Get a new authorization token")
        summary("Get authorization token")
        parameters(
            Parameter(
                name = "Authorization",
                `in` = Parameter.Location.header,
                schema = TypeDefinition.STRING,
                required = true,
                examples = mapOf(
                    "Digest access authentication" to Parameter.Example(
                        value = "Digest " +
                            "username=\"ReVanced\", " +
                            "realm=\"ReVanced\", " +
                            "nonce=\"abc123\", " +
                            "uri=\"/v${configuration.apiVersion}/token\", " +
                            "algorithm=SHA-256, " +
                            "response=\"yxz456\"",
                    ),
                ), // Provide an example for the header
            ),
        )
        response {
            description("The authorization token")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiToken>()
        }
        canRespondUnauthorized()
    }
}
