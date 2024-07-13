package app.revanced.api.configuration.routes

import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIAnnouncementArchivedAt
import app.revanced.api.configuration.schema.APIResponseAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import app.revanced.api.configuration.services.AnnouncementService
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PatchInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlin.time.Duration.Companion.minutes
import org.koin.ktor.ext.get as koinGet

internal fun Route.announcementsRoute() = route("announcements") {
    val announcementService = koinGet<AnnouncementService>()

    installCache(5.minutes)

    installAnnouncementsRouteDocumentation()

    rateLimit(RateLimitName("strong")) {
        get {
            call.respond(announcementService.all())
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("{channel}/latest") {
            installLatestChannelAnnouncementRouteDocumentation()

            get {
                val channel: String by call.parameters

                call.respondOrNotFound(announcementService.latest(channel))
            }

            route("id") {
                installLatestChannelAnnouncementIdRouteDocumentation()

                get {
                    val channel: String by call.parameters

                    call.respondOrNotFound(announcementService.latestId(channel))
                }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("{channel}") {
            installChannelAnnouncementsRouteDocumentation()

            get {
                val channel: String by call.parameters

                call.respond(announcementService.all(channel))
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("latest") {
            installLatestAnnouncementRouteDocumentation()

            get {
                call.respondOrNotFound(announcementService.latest())
            }

            route("id") {
                installLatestAnnouncementIdRouteDocumentation()

                get {
                    call.respondOrNotFound(announcementService.latestId())
                }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        authenticate("jwt") {
            installAnnouncementRouteDocumentation()

            post<APIAnnouncement> { announcement ->
                announcementService.new(announcement)
            }

            route("{id}") {
                installAnnouncementIdRouteDocumentation()

                patch<APIAnnouncement> { announcement ->
                    val id: Int by call.parameters

                    announcementService.update(id, announcement)
                }

                delete {
                    val id: Int by call.parameters

                    announcementService.delete(id)
                }

                route("archive") {
                    installAnnouncementArchiveRouteDocumentation()

                    post {
                        val id: Int by call.parameters
                        val archivedAt = call.receiveNullable<APIAnnouncementArchivedAt>()?.archivedAt

                        announcementService.archive(id, archivedAt)
                    }
                }

                route("unarchive") {
                    installAnnouncementUnarchiveRouteDocumentation()

                    post {
                        val id: Int by call.parameters

                        announcementService.unarchive(id)
                    }
                }
            }
        }
    }
}

private fun Route.installAnnouncementRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    post = PostInfo.builder {
        description("Create a new announcement")
        summary("Create announcement")
        request {
            requestType<APIAnnouncement>()
            description("The new announcement")
        }
        response {
            description("When the announcement was created")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
    }
}

private fun Route.installLatestAnnouncementRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get the latest announcement")
        summary("Get latest announcement")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The latest announcement")
            responseType<APIResponseAnnouncement>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("No announcement exists")
            responseType<Unit>()
        }
    }
}

private fun Route.installLatestAnnouncementIdRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get the id of the latest announcement")
        summary("Get id of latest announcement")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The id of the latest announcement")
            responseType<APIResponseAnnouncementId>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("No announcement exists")
            responseType<Unit>()
        }
    }
}

private fun Route.installChannelAnnouncementsRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "channel",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.STRING,
            description = "The channel to get the announcements from",
            required = true,
        ),
    )

    get = GetInfo.builder {
        description("Get the announcements from a channel")
        summary("Get announcements from channel")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The announcements in the channel")
            responseType<Set<APIResponseAnnouncement>>()
        }
    }
}

private fun Route.installAnnouncementArchiveRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "id",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.INT,
            description = "The id of the announcement to archive",
            required = true,
        ),
        Parameter(
            name = "archivedAt",
            `in` = Parameter.Location.query,
            schema = TypeDefinition.STRING,
            description = "The date and time the announcement to be archived",
            required = false,
        ),
    )

    post = PostInfo.builder {
        description("Archive an announcement")
        summary("Archive announcement")
        response {
            description("When the announcement was archived")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
    }
}

private fun Route.installAnnouncementUnarchiveRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "id",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.INT,
            description = "The id of the announcement to unarchive",
            required = true,
        ),
    )

    post = PostInfo.builder {
        description("Unarchive an announcement")
        summary("Unarchive announcement")
        response {
            description("When announcement was unarchived")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
    }
}

private fun Route.installAnnouncementIdRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "id",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.INT,
            description = "The id of the announcement to update",
            required = true,
        ),
    )

    patch = PatchInfo.builder {
        description("Update an announcement")
        summary("Update announcement")
        request {
            requestType<APIAnnouncement>()
            description("The new announcement")
        }
        response {
            description("When announcement was updated")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
    }

    delete = DeleteInfo.builder {
        description("Delete an announcement")
        summary("Delete announcement")
        response {
            description("When the announcement was deleted")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
    }
}

private fun Route.installAnnouncementsRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get the announcements")
        summary("Get announcements")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The announcements")
            responseType<Set<APIResponseAnnouncement>>()
        }
    }
}

private fun Route.installLatestChannelAnnouncementRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "channel",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.STRING,
            description = "The channel to get the latest announcement from",
            required = true,
        ),
    )

    get = GetInfo.builder {
        description("Get the latest announcement from a channel")
        summary("Get latest channel announcement")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The latest announcement in the channel")
            responseType<APIResponseAnnouncement>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("The channel does not exist")
            responseType<Unit>()
        }
    }
}

private fun Route.installLatestChannelAnnouncementIdRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "channel",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.STRING,
            description = "The channel to get the latest announcement id from",
            required = true,
        ),
    )

    get = GetInfo.builder {
        description("Get the id of the latest announcement from a channel")
        summary("Get id of latest announcement from channel")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The id of the latest announcement from the channel")
            responseType<APIResponseAnnouncementId>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("The channel does not exist")
            responseType<Unit>()
        }
    }
}
