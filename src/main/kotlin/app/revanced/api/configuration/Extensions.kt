package app.revanced.api.configuration

import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration

internal suspend fun ApplicationCall.respondOrNotFound(value: Any?) = respond(value ?: HttpStatusCode.NotFound)

internal fun ApplicationCallPipeline.installCache(maxAge: Duration) =
    installCache(CacheControl.MaxAge(maxAgeSeconds = maxAge.inWholeSeconds.toInt()))

internal fun ApplicationCallPipeline.installNoCache() =
    installCache(CacheControl.NoCache(null))

internal fun ApplicationCallPipeline.installCache(cacheControl: CacheControl) =
    install(CachingHeaders) {
        options { _, _ ->
            CachingOptions(cacheControl)
        }
    }

internal fun ApplicationCallPipeline.installNotarizedRoute(configure: NotarizedRoute.Config.() -> Unit = {}) =
    install(NotarizedRoute(), configure)

internal fun Route.staticFiles(
    remotePath: String,
    dir: Path,
    block: StaticContentConfig<File>.() -> Unit = {
        contentType {
            ContentType.Application.Json
        }
        extensions("json")
    },
) = staticFiles(remotePath, dir.toFile(), null, block)
