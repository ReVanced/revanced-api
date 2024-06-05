package app.revanced.api.configuration.routing.routes

import app.revanced.api.configuration.services.OldApiService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Route.oldApiRoute() {
    val oldApiService = get<OldApiService>()

    route(Regex("(v2|tools|contributor).*")) {
        handle {
            oldApiService.proxy(call)
        }
    }
}
