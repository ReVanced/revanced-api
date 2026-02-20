package app.revanced.api.server

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.*

internal fun Application.configureLogging() {
    install(CallLogging) {
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            "$status $httpMethod $uri"
        }
    }
}
