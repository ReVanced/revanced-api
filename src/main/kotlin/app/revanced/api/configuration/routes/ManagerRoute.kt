package app.revanced.api.configuration.routes

import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.schema.ApiManagerAsset
import app.revanced.api.configuration.schema.ApiRelease
import app.revanced.api.configuration.schema.ApiReleaseVersion
import app.revanced.api.configuration.services.ManagerService
import io.bkbn.kompendium.core.metadata.GetInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.managerRoute() = route("manager") {
    configure()

    // TODO: Remove this deprecated route eventually.
    route("latest") {
        configure(deprecated = true)
    }
}

private fun Route.configure(deprecated: Boolean = false) {
    val managerService = koinGet<ManagerService>()

    installManagerRouteDocumentation(deprecated)

    rateLimit(RateLimitName("weak")) {
        get {
            call.respond(managerService.latestRelease())
        }

        route("version") {
            installManagerVersionRouteDocumentation(deprecated)

            get {
                call.respond(managerService.latestVersion())
            }
        }
    }
}

private fun Route.installManagerRouteDocumentation(deprecated: Boolean) = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        if (deprecated) isDeprecated()
        description("Get the current manager release")
        summary("Get current manager release")
        response {
            description("The latest manager release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiRelease<ApiManagerAsset>>()
        }
    }
}

private fun Route.installManagerVersionRouteDocumentation(deprecated: Boolean) = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        if (deprecated) isDeprecated()
        description("Get the current manager release version")
        summary("Get current manager release version")
        response {
            description("The current manager release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiReleaseVersion>()
        }
    }
}
