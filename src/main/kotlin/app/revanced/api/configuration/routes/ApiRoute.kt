package app.revanced.api.configuration.routes

import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.services.ApiService
import app.revanced.api.configuration.services.AuthService
import io.ktor.http.*
import io.ktor.http.content.CachingOptions
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet

internal fun Route.rootRoute() {
    val apiService = koinGet<ApiService>()
    val authService = koinGet<AuthService>()

    rateLimit(RateLimitName("strong")) {
        authenticate("basic") {
            get("token") {
                call.respond(authService.newToken())
            }
        }

        route("contributors") {
            install(CachingHeaders) {
                options { _, _ ->
                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 1.days.inWholeSeconds.toInt()))
                }
            }

            get {
                call.respond(apiService.contributors())
            }
        }

        route("team") {
            install(CachingHeaders) {
                options { _, _ ->
                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 1.days.inWholeSeconds.toInt()))
                }
            }

            get {
                call.respond(apiService.team())
            }
        }
    }

    route("ping") {
        install(CachingHeaders) {
            options { _, _ ->
                CachingOptions(CacheControl.NoCache(null))
            }
        }

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
