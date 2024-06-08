package app.revanced.api.configuration.routes

import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIAnnouncementArchivedAt
import app.revanced.api.configuration.services.AnnouncementService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlin.time.Duration.Companion.minutes
import org.koin.ktor.ext.get as koinGet

internal fun Route.announcementsRoute() = route("announcements") {
    val announcementService = koinGet<AnnouncementService>()

    install(CachingHeaders) {
        options { _, _ ->
            CachingOptions(
                CacheControl.MaxAge(maxAgeSeconds = 1.minutes.inWholeSeconds.toInt()),
            )
        }
    }

    rateLimit(RateLimitName("weak")) {
        route("{channel}/latest") {
            get("id") {
                val channel: String by call.parameters

                call.respondOrNotFound(announcementService.latestId(channel))
            }

            get {
                val channel: String by call.parameters

                call.respondOrNotFound(announcementService.latest(channel))
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        get("{channel}") {
            val channel: String by call.parameters

            call.respond(announcementService.all(channel))
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("latest") {
            get("id") {
                call.respondOrNotFound(announcementService.latestId())
            }

            get {
                call.respondOrNotFound(announcementService.latest())
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        get {
            call.respond(announcementService.all())
        }
    }

    rateLimit(RateLimitName("strong")) {
        authenticate("jwt") {
            post {
                announcementService.new(call.receive<APIAnnouncement>())
            }

            post("{id}/archive") {
                val id: Int by call.parameters
                val archivedAt = call.receiveNullable<APIAnnouncementArchivedAt>()?.archivedAt

                announcementService.archive(id, archivedAt)
            }

            post("{id}/unarchive") {
                val id: Int by call.parameters

                announcementService.unarchive(id)
            }

            patch("{id}") {
                val id: Int by call.parameters
                val announcement = call.receive<APIAnnouncement>()

                announcementService.update(id, announcement)
            }

            delete("{id}") {
                val id: Int by call.parameters

                announcementService.delete(id)
            }
        }
    }
}
