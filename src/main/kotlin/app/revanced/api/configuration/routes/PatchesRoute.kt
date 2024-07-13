package app.revanced.api.configuration.routes

import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.schema.APIAssetPublicKeys
import app.revanced.api.configuration.schema.APIPatchesAsset
import app.revanced.api.configuration.schema.APIRelease
import app.revanced.api.configuration.schema.APIReleaseVersion
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

    route("latest") {
        installLatestPatchesRouteDocumentation()

        rateLimit(RateLimitName("weak")) {
            get {
                call.respond(patchesService.latestRelease())
            }

            route("version") {
                installLatestPatchesVersionRouteDocumentation()

                get {
                    call.respond(patchesService.latestVersion())
                }
            }
        }

        rateLimit(RateLimitName("strong")) {
            route("list") {
                installLatestPatchesListRouteDocumentation()

                get {
                    call.respondBytes(ContentType.Application.Json) { patchesService.list() }
                }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("keys") {
            installCache(356.days)

            installPatchesPublicKeyRouteDocumentation()

            get {
                call.respond(patchesService.publicKeys())
            }
        }
    }
}

private fun Route.installLatestPatchesRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the latest patches release")
        summary("Get latest patches release")
        response {
            description("The latest patches release")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIRelease<APIPatchesAsset>>()
        }
    }
}

private fun Route.installLatestPatchesVersionRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the latest patches release version")
        summary("Get latest patches release version")
        response {
            description("The latest patches release version")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIReleaseVersion>()
        }
    }
}

private fun Route.installLatestPatchesListRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Patches")

    get = GetInfo.builder {
        description("Get the list of patches from the latest patches release")
        summary("Get list of patches from latest patches release")
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
        description("Get the public keys for verifying patches and integrations assets")
        summary("Get patches and integrations public keys")
        response {
            description("The public keys")
            mediaTypes("application/json")
            responseCode(HttpStatusCode.OK)
            responseType<APIAssetPublicKeys>()
        }
    }
}
