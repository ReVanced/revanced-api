package app.revanced.api.configuration.routing.routes

import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.services.ApiService
import app.revanced.api.configuration.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Route.rootRoute() {
    val apiService = get<ApiService>()
    val authService = get<AuthService>()

    rateLimit(RateLimitName("strong")) {
        authenticate("basic") {
            get("token") {
                call.respond(authService.newToken())
            }
        }

        get("contributors") {
            call.respond(apiService.contributors())
        }

        get("team") {
            call.respond(apiService.team())
        }
    }

    route("ping") {
        handle {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    rateLimit(RateLimitName("weak")) {
        get("backend/rate_limit") {
            call.respondOrNotFound(apiService.rateLimit())
        }

        staticResources("/", "/app/revanced/api/static") {
            contentType { ContentType.Application.Json }
            extensions("json")
        }
    }
}
