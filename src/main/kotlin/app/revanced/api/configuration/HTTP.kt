package app.revanced.api.configuration

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP(
    allowedHost: String,
) {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHost(allowedHost)
    }
    install(CachingHeaders) {
        options { _, _ -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 5.minutes.inWholeSeconds.toInt())) }
    }
    install(RateLimit) {
        register(RateLimitName("weak")) {
            rateLimiter(limit = 30, refillPeriod = 2.minutes)
            requestKey { it.request.origin.remoteAddress }
        }
        register(RateLimitName("strong")) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
            requestKey { it.request.origin.remoteHost }
        }
    }
}
