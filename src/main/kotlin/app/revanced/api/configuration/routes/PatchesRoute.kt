package app.revanced.api.configuration.routes

import app.revanced.api.configuration.ApiAssetPublicKey
import app.revanced.api.configuration.ApiRelease
import app.revanced.api.configuration.ApiReleaseVersion
import app.revanced.api.configuration.installCache
import app.revanced.api.configuration.services.PatchesService
import io.ktor.http.*
import io.ktor.openapi.Parameters
import io.ktor.openapi.jsonSchema
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.describe
import kotlin.time.Duration.Companion.days
import org.koin.ktor.ext.get as koinGet

internal fun Route.patchesRoute() = route("patches") {
    val patchesService = koinGet<PatchesService>()

    rateLimit(RateLimitName("weak")) {
        get {
            val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

            call.respond(patchesService.latestRelease(prerelease))
        }.describe {
            description = "Get the current patches release"
            summary = "Get current patches release"

            parameters {
                prereleaseParameter
            }

            responses {
                HttpStatusCode.OK {
                    description = "The current patches release"
                    schema = jsonSchema<ApiRelease>()
                    ContentType.Application.Json()
                }
            }
        }

        route("version") {
            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respond(patchesService.latestVersion(prerelease))
            }.describe {
                description = "Get the current patches release version"
                summary = "Get current patches release version"

                parameters {
                    prereleaseParameter
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The current patches release version"
                        schema = jsonSchema<ApiReleaseVersion>()
                        ContentType.Application.Json()
                    }
                }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("list") {
            get {
                val prerelease = call.parameters["prerelease"]?.toBoolean() ?: false

                call.respondBytes(ContentType.Application.Json) { patchesService.list(prerelease) }
            }.describe {
                description = "Get the list of patches from the current patches release"
                summary = "Get list of patches from current patches release"

                parameters {
                    prereleaseParameter
                }

                responses {
                    HttpStatusCode.OK {
                        description = "The list of patches"
                        ContentType.Application.Json
                    }
                }
            }
        }
    }

    rateLimit(RateLimitName("strong")) {
        route("keys") {
            installCache(356.days)

            get {
                call.respond(patchesService.publicKey())
            }.describe {
                description = "Get the public keys for verifying patches assets"
                summary = "Get patches public keys"

                responses {
                    HttpStatusCode.OK {
                        description = "The public keys"
                        schema = jsonSchema<ApiAssetPublicKey>()
                        ContentType.Application.Json()
                    }
                }
            }
        }
    }
}.describe {
    tag("Patches")
}

private val Parameters.Builder.prereleaseParameter
    get() = query("prerelease") {
        description = "Whether to get the current patches prerelease"
        required = false
    }
