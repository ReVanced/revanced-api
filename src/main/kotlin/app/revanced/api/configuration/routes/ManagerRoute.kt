package app.revanced.api.configuration.routes

import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.schema.APIManagerAsset
import app.revanced.api.configuration.schema.APIRelease
import app.revanced.api.configuration.schema.APIReleaseVersion
import app.revanced.api.configuration.services.ManagerService
import io.bkbn.kompendium.core.metadata.GetInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.managerRoute() = route("manager") {
    val managerService = koinGet<ManagerService>()

    route("latest") {
        installLatestManagerRouteDocumentation()

        rateLimit(RateLimitName("weak")) {
            get {
                call.respond(managerService.latestRelease())
            }

            route("version") {
                installLatestManagerVersionRouteDocumentation()

                get {
                    call.respond(managerService.latestVersion())
                }
            }
        }
    }
}

private fun Route.installLatestManagerRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the latest manager release")
        summary("Get latest manager release")
        response {
            description("The latest manager release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIRelease<APIManagerAsset>>()
        }
    }
}

private fun Route.installLatestManagerVersionRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the latest manager release version")
        summary("Get latest manager release version")
        response {
            description("The latest manager release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIReleaseVersion>()
        }
    }
}
