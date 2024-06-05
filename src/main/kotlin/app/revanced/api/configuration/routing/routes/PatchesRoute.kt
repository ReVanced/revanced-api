package app.revanced.api.configuration.routing.routes

import app.revanced.api.services.PatchesService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.configurePatchesRoute() = route("/patches") {
    val patchesService = koinGet<PatchesService>()

    route("latest") {
        get {
            call.respond(patchesService.latestRelease())
        }

        get("/version") {
            call.respond(patchesService.latestVersion())
        }

        get("/list") {
            call.respondBytes(ContentType.Application.Json) { patchesService.list() }
        }
    }
}
