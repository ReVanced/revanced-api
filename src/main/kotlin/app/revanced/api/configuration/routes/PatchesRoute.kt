package app.revanced.api.configuration.routes

import app.revanced.api.configuration.services.PatchesService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.patchesRoute() = route("patches") {
    val patchesService = koinGet<PatchesService>()

    route("latest") {
        rateLimit(RateLimitName("weak")) {
            get {
                call.respond(patchesService.latestRelease())
            }

            get("version") {
                call.respond(patchesService.latestVersion())
            }
        }

        rateLimit(RateLimitName("strong")) {
            get("list") {
                call.respondBytes(ContentType.Application.Json) { patchesService.list() }
            }
        }
    }
}
