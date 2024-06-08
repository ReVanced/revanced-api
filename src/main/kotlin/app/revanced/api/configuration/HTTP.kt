package app.revanced.api.configuration

import app.revanced.api.configuration.repository.ConfigurationRepository
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP() {
    val configurationRepository = get<ConfigurationRepository>()

    install(CORS) {
        allowHost(host = configurationRepository.host)
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
