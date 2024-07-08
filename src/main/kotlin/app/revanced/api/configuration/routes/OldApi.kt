package app.revanced.api.configuration.routes

import app.revanced.api.configuration.services.OldApiService
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Route.oldApiRoute() {
    val oldApiService = get<OldApiService>()

    rateLimit(RateLimitName("weak")) {
        route(Regex("/(v2|tools|contributors).*")) {
            handle {
                oldApiService.proxy(call)
            }
        }
    }
}
