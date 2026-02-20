package app.revanced.api.server.routes

import app.revanced.api.server.ApiRelease
import app.revanced.api.server.ApiReleaseHistory
import app.revanced.api.server.ApiReleaseVersion
import app.revanced.api.server.services.ManagerService
import io.ktor.http.*
import io.ktor.openapi.Parameters
import io.ktor.openapi.jsonSchema
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.get as koinGet

internal fun Route.managerRoute() = route("manager") {
    val managerService = koinGet<ManagerService>()

    rateLimit(RateLimitName("weak")) {
        get {
            val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

            call.respond(managerService.latestRelease(prerelease))
        }.describe {
            description = "Get the current manager release"
            summary = "Get current manager release"

            parameters {
                prereleaseParameter
            }

            responses {
                HttpStatusCode.OK {
                    description = "The latest manager release"
                    schema = jsonSchema<ApiRelease>()
                    ContentType.Application.Json()
                }
            }
        }

        get("history") {
            val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false
            val history = managerService.history(prerelease)

            if (history != null) call.respond(history)
            else call.respond(HttpStatusCode.NotFound)
        }.describe {
            description = "Get the manager release history"
            summary = "Get manager release history"

            parameters {
                prereleaseParameter
            }

            responses {
                HttpStatusCode.OK {
                    description = "The manager release history"
                    schema = jsonSchema<ApiReleaseHistory>()
                    ContentType.Application.Json()
                }
                HttpStatusCode.NotFound {
                    description = "No manager release history found"
                }
            }
        }

        route("version") {
            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respond(managerService.latestVersion(prerelease))
            }.describe {
                description = "Get the current manager release version"
                summary = "Get current manager release version"

                parameters {
                    prereleaseParameter
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The current manager release version"
                        schema = jsonSchema<ApiReleaseVersion>()
                        ContentType.Application.Json()
                    }
                }
            }
        }

        route("downloaders") {
            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respond(managerService.latestDownloadersRelease(prerelease))
            }.describe {
                description = "Get the current manager downloaders release"
                summary = "Get current manager downloaders release"

                parameters {
                    prereleaseParameter
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The latest manager downloaders release"
                        schema = jsonSchema<ApiRelease>()
                        ContentType.Application.Json()
                    }
                }
            }

            route("version") {
                get {
                    val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                    call.respond(managerService.latestDownloadersVersion(prerelease))
                }.describe {
                    description = "Get the current manager downloaders release version"
                    summary = "Get current manager downloaders release version"

                    parameters {
                        prereleaseParameter
                    }

                    responses {
                        HttpStatusCode.OK {
                            description = "The current manager downloaders release version"
                            schema = jsonSchema<ApiReleaseVersion>()
                            ContentType.Application.Json()
                        }
                    }
                }
            }
        }
    }
}.describe {
    tag("Manager")
}

private val Parameters.Builder.prereleaseParameter
    get() = query("prerelease") {
        description = "Whether to get the current manager prerelease"
        required = false
    }
