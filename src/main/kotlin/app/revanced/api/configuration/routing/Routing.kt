package app.revanced.api.configuration.routing

import app.revanced.api.configuration.routing.routes.announcementsRoute
import app.revanced.api.configuration.routing.routes.oldApiRoute
import app.revanced.api.configuration.routing.routes.patchesRoute
import app.revanced.api.configuration.routing.routes.rootRoute
import app.revanced.api.repository.ConfigurationRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Application.configureRouting() = routing {
    val configuration = get<ConfigurationRepository>()

    route("/v${configuration.apiVersion}") {
        rootRoute()
        patchesRoute()
        announcementsRoute()
    }

    // TODO: Remove, once migration period from v2 API is over (In 1-2 years).
    oldApiRoute()
}
