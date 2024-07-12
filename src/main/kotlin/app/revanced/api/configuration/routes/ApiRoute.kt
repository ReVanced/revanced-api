package app.revanced.api.configuration.routes

import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNoCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.schema.APIContributable
import app.revanced.api.configuration.schema.APIMember
import app.revanced.api.configuration.schema.APIRateLimit
import app.revanced.api.configuration.services.ApiService
import app.revanced.api.configuration.services.AuthService
import io.bkbn.kompendium.core.metadata.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet

internal fun Route.apiRoute() {
    val apiService = koinGet<ApiService>()
    val authService = koinGet<AuthService>()

    rateLimit(RateLimitName("strong")) {
        authenticate("auth-digest") {
            route("token") {
                installTokenRouteDocumentation()

                get {
                    call.respond(authService.newToken())
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

    route("ping") {
        installNoCache()

        installPingRouteDocumentation()

        head {
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

        staticResources("/", "/app/revanced/api/static/versioned") {
            contentType { ContentType.Application.Json }
            extensions("json")
        }
    }
}

fun Route.installRateLimitRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get the rate limit of the backend")
        summary("Get rate limit of backend")
        response {
            description("The rate limit of the backend")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIRateLimit>()
        }
    }
}

fun Route.installPingRouteDocumentation() = installNotarizedRoute {
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

fun Route.installTeamRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get the list of team members")
        summary("Get team members")
        response {
            description("The list of team members")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<Set<APIMember>>()
        }
    }
}

fun Route.installContributorsRouteDocumentation() = installNotarizedRoute {
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

fun Route.installTokenRouteDocumentation() = installNotarizedRoute {
    tags = setOf("API")

    get = GetInfo.builder {
        description("Get a new authorization token")
        summary("Get authorization token")
        response {
            description("The authorization token")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<String>()
        }
    }
}
