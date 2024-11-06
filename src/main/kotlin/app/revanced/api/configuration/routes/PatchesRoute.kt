package app.revanced.api.configuration.routes

import app.revanced.api.configuration.ApiAssetPublicKey
import app.revanced.api.configuration.ApiRelease
import app.revanced.api.configuration.ApiReleaseVersion
import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.services.PatchesService
import io.bkbn.kompendium.core.metadata.GetInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet

internal fun Route.patchesRoute() = route("patches") {
    val patchesService = koinGet<PatchesService>()

    installPatchesRouteDocumentation()

    rateLimit(RateLimitName("weak")) {
        get {
            call.respond(patchesService.latestRelease())
        }

        route("version") {
            installPatchesVersionRouteDocumentation()

            get {
                call.respond(patchesService.latestVersion())
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("list") {
            installPatchesListRouteDocumentation()

            get {
                call.respondBytes(ContentType.Application.Json) { patchesService.list() }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("keys") {
            installCache(356.days)

            installPatchesPublicKeyRouteDocumentation()

            get {
                call.respond(patchesService.publicKey())
            }
        }
    }
}

private fun Route.installPatchesRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the current patches release")
        summary("Get current patches release")
        response {
            description("The current patches release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiRelease>()
        }
    }
}

private fun Route.installPatchesVersionRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the current patches release version")
        summary("Get current patches release version")
        response {
            description("The current patches release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiReleaseVersion>()
        }
    }
}

private fun Route.installPatchesListRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the list of patches from the current patches release")
        summary("Get list of patches from current patches release")
        response {
            description("The list of patches")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<String>()
        }
    }
}

private fun Route.installPatchesPublicKeyRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the public keys for verifying patches assets")
        summary("Get patches public keys")
        response {
            description("The public keys")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<ApiAssetPublicKey>()
        }
    }
}
