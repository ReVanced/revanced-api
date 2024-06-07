package app.revanced.api.configuration

import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.routes.announcementsRoute
import app.revanced.api.configuration.routes.oldApiRoute
import app.revanced.api.configuration.routes.patchesRoute
import app.revanced.api.configuration.routes.rootRoute
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
