package app.revanced.api.configuration

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import kotlin.time.Duration.Companion.minutes

fun Application.configureHTTP(
    allowedHost: String,
) {
    install(ConditionalHeaders)
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHost(allowedHost)
    }
    install(CachingHeaders) {
        options { _, _ -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 5.minutes.inWholeSeconds.toInt())) }
    }
}
