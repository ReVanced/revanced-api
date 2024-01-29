package app.revanced.api.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP() {
    install(ConditionalHeaders)
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(CachingHeaders) {
        options { _, _ ->
            CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 5.minutes.inWholeSeconds.toInt()))
        }
    }
}
