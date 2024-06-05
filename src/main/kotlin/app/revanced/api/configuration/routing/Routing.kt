package app.revanced.api.configuration.routing

import app.revanced.api.configuration.routing.routes.configureAnnouncementsRoute
import app.revanced.api.configuration.routing.routes.configurePatchesRoute
import app.revanced.api.configuration.routing.routes.configureRootRoute
import app.revanced.api.repository.ConfigurationRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Application.configureRouting() = routing {
    val configuration = get<ConfigurationRepository>()

    route("/v${configuration.apiVersion}") {
        configureRootRoute()
        configurePatchesRoute()
        configureAnnouncementsRoute()
    }
}
