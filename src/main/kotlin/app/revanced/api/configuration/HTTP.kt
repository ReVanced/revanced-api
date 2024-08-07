package app.revanced.api.configuration

import app.revanced.api.configuration.repository.ConfigurationRepository
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP() {
    val configurationRepository = get<ConfigurationRepository>()

    install(CORS) {
        configurationRepository.corsAllowedHosts.forEach { host ->
            allowHost(
                host = host,
                schemes = listOf("http", "https")
            )
        }
    }

    install(RateLimit) {
        fun rateLimit(name: String, block: RateLimitProviderConfig.() -> Unit) = register(RateLimitName(name)) {
            requestKey {
                it.request.uri + it.request.origin.remoteAddress
            }

            block()
        }

        rateLimit("weak") {
            rateLimiter(limit = 30, refillPeriod = 2.minutes)
        }
        rateLimit("strong") {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
        }
    }
}
