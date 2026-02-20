package app.revanced.api.server.routes

import app.revanced.api.server.ApiAnnouncement
import app.revanced.api.server.ApiResponseAnnouncement
import app.revanced.api.server.ApiResponseAnnouncementId
import app.revanced.api.server.installCache
import app.revanced.api.server.respondOrNotFound
import app.revanced.api.server.services.AnnouncementService
import io.ktor.http.*
import io.ktor.openapi.ExampleObject
import io.ktor.openapi.Parameters
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import io.ktor.server.util.*
import kotlin.time.Duration.Companion.minutes
import org.koin.ktor.ext.get as koinGet

internal fun Route.announcementsRoute() = route("announcements") {
    val announcementService = koinGet<AnnouncementService>()

    installCache(5.minutes)

    rateLimit(RateLimitName("strong")) {
        get {
            val cursor = call.parameters["cursor"]?.toInt() ?: Int.MAX_VALUE
            val count = call.parameters["count"]?.toInt() ?: 16
            val tags = call.parameters.getAll("tag")

            call.respond(announcementService.paged(cursor, count, tags?.toSet()))
        }.describe {
            description = "Get a page of announcements"
            summary = "Get announcements"

            parameters {
                query("cursor") {
                    description =
                        "The offset of the announcements. Default is Int.MAX_VALUE (Newest first)"
                    required = false
                }
                query("count") {
                    description = "The count of the announcements. Default is 16"
                    required = false
                }
                query("tag") {
                    description = "The tags to filter the announcements by. Default is all tags"
                    required = false
                }
            }

            responses {
                HttpStatusCode.OK {
                    description = "The announcements"
                    schema = jsonSchema<Set<ApiResponseAnnouncement>>()
                    ContentType.Application.Json()
                }
            }
        }
    }

    rateLimit(RateLimitName("weak")) {
        authenticate("jwt") {
            post<ApiAnnouncement> { announcement ->
                announcementService.new(announcement)

                call.respond(HttpStatusCode.OK)
            }.describe {
                description = "Create a new announcement"
                summary = "Create announcement"

                parameters {
                    authHeaderParameter
                }

                requestBody {
                    description = "The new announcement"
                    schema = jsonSchema<ApiAnnouncement>()
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The announcement is created"
                    }
                    HttpStatusCode.Unauthorized()
                }
            }
        }

        route("latest") {
            get {
                val tags = call.parameters.getAll("tag")

                if (tags?.isNotEmpty() == true) {
                    call.respond(announcementService.latest(tags.toSet()))
                } else {
                    call.respondOrNotFound(announcementService.latest())
                }
            }.describe {
                description = "Get the latest announcement"
                summary = "Get latest announcement"

                parameters {
                    query("tag") {
                        description = "The tags to filter the latest announcements by"
                        required = false
                    }
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The latest announcement"
                        schema = jsonSchema<ApiResponseAnnouncement>()
                        ContentType.Application.Json()
                    }
                    HttpStatusCode.OK {
                        description = "The latest announcements"
                        schema = jsonSchema<Set<ApiResponseAnnouncement>>()
                        ContentType.Application.Json()
                    }
                    HttpStatusCode.NotFound {
                        description = "No announcement exists"
                    }
                }
            }
        }

        route("id") {
            get {
                val tags = call.parameters.getAll("tag")

                if (tags?.isNotEmpty() == true) {
                    call.respond(announcementService.latestId(tags.toSet()))
                } else {
                    call.respondOrNotFound(announcementService.latestId())
                }
            }
        }.describe {
            description = "Get the ID of the latest announcement"
            summary = "Get ID of latest announcement"

            parameters {
                query("tag") {
                    description = "The tags to filter the latest announcements by"
                    required = false
                }
            }

            responses {
                HttpStatusCode.OK {
                    description = "The ID of the latest announcement"
                    schema = jsonSchema<ApiResponseAnnouncementId>()
                    ContentType.Application.Json()
                }
                HttpStatusCode.OK {
                    description = "The IDs of the latest announcements"
                    schema = jsonSchema<Set<ApiResponseAnnouncement>>()
                    ContentType.Application.Json()
                }
                HttpStatusCode.NotFound {
                    description = "No announcement exists"
                }
            }
        }


        route("{id}") {
            get {
                val id: Int by call.parameters

                call.respondOrNotFound(announcementService.get(id))
            }.describe {
                description = "Get an announcement"
                summary = "Get announcement"

                parameters {
                    path("id") {
                        description = "The ID of the announcement to get"
                        required = true
                    }
                    authHeaderParameter
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The announcement"
                        schema = jsonSchema<ApiResponseAnnouncement>()
                        ContentType.Application.Json()
                    }
                    HttpStatusCode.NotFound {
                        description = "The announcement does not exist"
                    }
                }
            }

            authenticate("jwt") {
                patch<ApiAnnouncement> { announcement ->
                    val id: Int by call.parameters

                    announcementService.update(id, announcement)

                    call.respond(HttpStatusCode.OK)
                }.describe {
                    description = "Update an announcement"
                    summary = "Update announcement"

                    parameters {
                        path("id") {
                            description = "The ID of the announcement to update"
                            required = true
                        }
                        authHeaderParameter
                    }

                    requestBody {
                        description = "The new announcement"
                        schema = jsonSchema<ApiAnnouncement>()
                    }

                    responses {
                        HttpStatusCode.OK {
                            description = "The announcement is updated"
                        }
                        HttpStatusCode.Unauthorized()
                    }
                }

                delete {
                    val id: Int by call.parameters

                    announcementService.delete(id)

                    call.respond(HttpStatusCode.OK)
                }.describe {
                    description = "Delete an announcement"
                    summary = "Delete announcement"

                    parameters {
                        path("id") {
                            description = "The ID of the announcement to delete"
                            required = true
                        }
                        authHeaderParameter
                    }

                    responses {
                        HttpStatusCode.OK {
                            description = "The announcement is deleted"
                        }
                        HttpStatusCode.Unauthorized()
                    }
                }
            }
        }

        route("tags") {
            get {
                call.respond(announcementService.tags())
            }.describe {
                description = "Get all announcement tags"
                summary = "Get announcement tags"

                responses {
                    HttpStatusCode.OK {
                        description = "The announcement tags"
                        schema = jsonSchema<Set<String>>()
                        ContentType.Application.Json()
                    }
                }
            }
        }
    }
}.describe {
    tag("Announcements")
}

private val Parameters.Builder.authHeaderParameter
    get() = header("Authorization") {
        description = "Whether to get the current manager prerelease"
        required = false
        example("Bearer authentication", ExampleObject("Bearer abc123"))
    }
