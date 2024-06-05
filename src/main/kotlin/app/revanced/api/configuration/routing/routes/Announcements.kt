package app.revanced.api.configuration.routing.routes

import app.revanced.api.schema.APIAnnouncement
import app.revanced.api.schema.APIAnnouncementArchivedAt
import app.revanced.api.services.AnnouncementService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.get as koinGet

internal fun Route.configureAnnouncementsRoute() = route("/announcements") {
    val announcementService = koinGet<AnnouncementService>()

    route("/{channel}/latest") {
        get("/id") {
            val channel: String by call.parameters

            call.respond(
                announcementService.latestId(channel) ?: return@get call.respond(HttpStatusCode.NotFound),
            )
        }

        get {
            val channel: String by call.parameters

            call.respond(
                announcementService.latest(channel) ?: return@get call.respond(HttpStatusCode.NotFound),
            )
        }
    }

    get("/{channel}") {
        val channel: String by call.parameters

        call.respond(announcementService.all(channel))
    }

    route("/latest") {
        get("/id") {
            call.respond(announcementService.latestId() ?: return@get call.respond(HttpStatusCode.NotFound))
        }

        get {
            call.respond(announcementService.latest() ?: return@get call.respond(HttpStatusCode.NotFound))
        }
    }

    get {
        call.respond(announcementService.all())
    }

    authenticate("jwt") {
        post {
            announcementService.new(call.receive<APIAnnouncement>())
        }

        post("/{id}/archive") {
            val id: Int by call.parameters
            val archivedAt = call.receiveNullable<APIAnnouncementArchivedAt>()?.archivedAt

            announcementService.archive(id, archivedAt)
        }

        post("/{id}/unarchive") {
            val id: Int by call.parameters

            announcementService.unarchive(id)
        }

        patch("/{id}") {
            val id: Int by call.parameters

            announcementService.update(id, call.receive<APIAnnouncement>())
        }

        delete("/{id}") {
            val id: Int by call.parameters

            announcementService.delete(id)
        }
    }
}
