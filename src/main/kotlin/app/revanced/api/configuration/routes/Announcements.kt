package app.revanced.api.configuration.routes

import app.revanced.api.configuration.canRespondUnauthorized
import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.installNotarizedRoute
import app.revanced.api.configuration.respondOrNotFound
import app.revanced.api.configuration.schema.ApiAnnouncement
import app.revanced.api.configuration.schema.ApiResponseAnnouncement
import app.revanced.api.configuration.schema.ApiResponseAnnouncementId
import app.revanced.api.configuration.services.AnnouncementService
import io.bkbn.kompendium.core.metadata.*
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
            val offset = call.parameters["offset"]?.toInt() ?: 0
            val count = call.parameters["count"]?.toInt() ?: 16
            val tags = call.parameters.getAll("tag")

            call.respond(announcementService.paged(offset, count, tags?.map { it.toInt() }?.toSet()))
        }
    }

    rateLimit(RateLimitName("weak")) {
        authenticate("jwt") {
            post<ApiAnnouncement> { announcement ->
                announcementService.new(announcement)

                call.respond(HttpStatusCode.OK)
            }
        }

        route("latest") {
            installAnnouncementsLatestRouteDocumentation()

            get {
                val tags = call.parameters.getAll("tag")

                if (tags?.isNotEmpty() == true) {
                    call.respond(announcementService.latest(tags.map { it.toInt() }.toSet()))
                } else {
                    call.respondOrNotFound(announcementService.latest())
                }
            }

            route("id") {
                installAnnouncementsLatestIdRouteDocumentation()

                get {
                    val tags = call.parameters.getAll("tag")

                    if (tags?.isNotEmpty() == true) {
                        call.respond(announcementService.latestId(tags.map { it.toInt() }.toSet()))
                    } else {
                        call.respondOrNotFound(announcementService.latestId())
                    }
                }
            }
        }

        route("{id}") {
            installAnnouncementsIdRouteDocumentation()

            get {
                val id: Int by call.parameters

                call.respondOrNotFound(announcementService.get(id))
            }

            authenticate("jwt") {
                patch<ApiAnnouncement> { announcement ->
                    val id: Int by call.parameters

                    announcementService.update(id, announcement)

                    call.respond(HttpStatusCode.OK)
                }

                delete {
                    val id: Int by call.parameters

                    announcementService.delete(id)

                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        route("tags") {
            installAnnouncementsTagsRouteDocumentation()

            get {
                call.respond(announcementService.tags())
            }
        }
    }
}

private val authHeaderParameter = Parameter(
    name = "Authorization",
    `in` = Parameter.Location.header,
    schema = TypeDefinition.STRING,
    required = true,
    examples = mapOf("Bearer authentication" to Parameter.Example("Bearer abc123")),
)

private fun Route.installAnnouncementsRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get a page of announcements")
        summary("Get announcements")
        parameters(
            Parameter(
                name = "offset",
                `in` = Parameter.Location.query,
                schema = TypeDefinition.INT,
                description = "The offset of the announcements",
                required = false,
            ),
            Parameter(
                name = "count",
                `in` = Parameter.Location.query,
                schema = TypeDefinition.INT,
                description = "The count of the announcements",
                required = false,
            ),
            Parameter(
                name = "tag",
                `in` = Parameter.Location.query,
                schema = TypeDefinition.INT,
                description = "The tag IDs to filter the announcements by",
                required = false,
            ),
        )
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The announcements")
            responseType<Set<ApiResponseAnnouncement>>()
        }
    }

    post = PostInfo.builder {
        description("Create a new announcement")
        summary("Create announcement")
        parameters(authHeaderParameter)
        request {
            requestType<ApiAnnouncement>()
            description("The new announcement")
        }
        response {
            description("The announcement is created")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
        canRespondUnauthorized()
    }
}

private fun Route.installAnnouncementsLatestRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get the latest announcement")
        summary("Get latest announcement")
        parameters(
            Parameter(
                name = "tag",
                `in` = Parameter.Location.query,
                schema = TypeDefinition.INT,
                description = "The tag IDs to filter the latest announcements by",
                required = false,
            ),
        )
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The latest announcement")
            responseType<ApiResponseAnnouncement>()
        }
        canRespond {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The latest announcements")
            responseType<Set<ApiResponseAnnouncement>>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("No announcement exists")
            responseType<Unit>()
        }
    }
}

private fun Route.installAnnouncementsLatestIdRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get the ID of the latest announcement")
        summary("Get ID of latest announcement")
        parameters(
            Parameter(
                name = "tag",
                `in` = Parameter.Location.query,
                schema = TypeDefinition.INT,
                description = "The tag IDs to filter the latest announcements by",
                required = false,
            ),
        )
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The ID of the latest announcement")
            responseType<ApiResponseAnnouncementId>()
        }
        canRespond {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The IDs of the latest announcements")
            responseType<Set<ApiResponseAnnouncement>>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("No announcement exists")
            responseType<Unit>()
        }
    }
}

private fun Route.installAnnouncementsIdRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    parameters = listOf(
        Parameter(
            name = "id",
            `in` = Parameter.Location.path,
            schema = TypeDefinition.INT,
            description = "The ID of the announcement to update",
            required = true,
        ),
        authHeaderParameter,
    )

    get = GetInfo.builder {
        description("Get an announcement")
        summary("Get announcement")
        response {
            description("The announcement")
            responseCode(HttpStatusCode.OK)
            responseType<ApiResponseAnnouncement>()
        }
        canRespond {
            responseCode(HttpStatusCode.NotFound)
            description("The announcement does not exist")
            responseType<Unit>()
        }
    }

    patch = PatchInfo.builder {
        description("Update an announcement")
        summary("Update announcement")
        request {
            requestType<ApiAnnouncement>()
            description("The new announcement")
        }
        response {
            description("The announcement is updated")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
        canRespondUnauthorized()
    }

    delete = DeleteInfo.builder {
        description("Delete an announcement")
        summary("Delete announcement")
        response {
            description("The announcement is deleted")
            responseCode(HttpStatusCode.OK)
            responseType<Unit>()
        }
        canRespondUnauthorized()
    }
}

private fun Route.installAnnouncementsTagsRouteDocumentation() = installNotarizedRoute {
    tags = setOf("Announcements")

    get = GetInfo.builder {
        description("Get all announcement tags")
        summary("Get announcement tags")
        response {
            responseCode(HttpStatusCode.OK)
            mediaTypes("application/json")
            description("The announcement tags")
            responseType<Set<String>>()
        }
    }
}
