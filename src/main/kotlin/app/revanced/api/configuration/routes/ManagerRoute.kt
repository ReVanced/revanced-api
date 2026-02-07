package app.revanced.api.configuration.routes

import app.revanced.api.configuration.ApiRelease
import app.revanced.api.configuration.ApiReleaseVersion
import app.revanced.api.configuration.services.ManagerService
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
