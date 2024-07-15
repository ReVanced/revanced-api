package app.revanced.api.configuration

import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.routes.*
import app.revanced.api.configuration.routes.announcementsRoute
import app.revanced.api.configuration.routes.apiRoute
import app.revanced.api.configuration.routes.oldApiRoute
import app.revanced.api.configuration.routes.patchesRoute
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.core.routes.swagger
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes
import org.koin.ktor.ext.get as koinGet

internal fun Application.configureRouting() = routing {
    val configuration = koinGet<ConfigurationRepository>()

    installCache(5.minutes)

    route("/v${configuration.apiVersion}") {
        announcementsRoute()
        patchesRoute()
        managerRoute()
        apiRoute()
    }

    staticFiles("/", configuration.staticFilesPath)

    swagger(pageTitle = "ReVanced API", path = "/")
    redoc(pageTitle = "ReVanced API", path = "/redoc")

    // TODO: Remove, once migration period from v2 API is over (In 1-2 years).
    oldApiRoute()
}
