package app.revanced.api.configuration.routes

import app.revanced.api.configuration.ApiRelease
import app.revanced.api.configuration.ApiReleaseVersion
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.services.ManagerService
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.managerRoute() = route("manager") {
    val managerService = koinGet<ManagerService>()

    installManagerRouteDocumentation()

    rateLimit(RateLimitName("weak")) {
        get {
            val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

            call.respond(managerService.latestRelease(prerelease))
        }

        route("version") {
            installManagerVersionRouteDocumentation()

            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respond(managerService.latestVersion(prerelease))
            }
        }
        
        route("downloaders") {
            installManagerDownloadersRouteDocumentation()
            
            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respond(managerService.latestDownloadersRelease(prerelease))
            }

            route("version") {
                installManagerDownloadersVersionRouteDocumentation()
                
                get {
                    val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                    call.respond(managerService.latestDownloadersVersion(prerelease))
                }
            }
        }
    }
}

private val prereleaseParameter = Parameter(
    name = "prerelease",
    `in` = Parameter.Location.query,
    schema = TypeDefinition.STRING,
    description = "Whether to get the current manager prerelease",
    required = false,
)

private fun Route.installManagerRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the current manager release")
        summary("Get current manager release")
        parameters(prereleaseParameter)
        response {
            description("The latest manager release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiRelease>()
        }
    }
}

private fun Route.installManagerVersionRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the current manager release version")
        summary("Get current manager release version")
        parameters(prereleaseParameter)
        response {
            description("The current manager release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiReleaseVersion>()
        }
    }
}

private fun Route.installManagerDownloadersRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the current manager downloaders release")
        summary("Get current manager downloaders release")
        parameters(prereleaseParameter)
        response {
            description("The latest manager downloaders release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiRelease>()
        }
    }
}

private fun Route.installManagerDownloadersVersionRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Manager")

    get = GetInfo.builder {
        description("Get the current manager downloaders release version")
        summary("Get current manager downloaders release version")
        parameters(prereleaseParameter)
        response {
            description("The current manager downloaders release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiReleaseVersion>()
        }
    }
}