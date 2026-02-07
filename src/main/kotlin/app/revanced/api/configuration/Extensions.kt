package app.revanced.api.configuration

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

internal suspend fun ApplicationCall.respondOrNotFound(value: Any?) =
    respond(value ?: HttpStatusCode.NotFound)

internal fun Route.installCache(maxAge: Duration) =
    installCache(CacheControl.MaxAge(maxAgeSeconds = maxAge.inWholeSeconds.toInt()))

internal fun Route.installNoCache() =
    installCache(CacheControl.NoCache(null))

internal fun Route.installCache(cacheControl: CacheControl) =
    install(CachingHeaders) {
        options { _, _ ->
            CachingOptions(cacheControl)
        }
    }

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
