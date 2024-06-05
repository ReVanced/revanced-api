package app.revanced.api.configuration.routing.routes

import app.revanced.api.services.ApiService
import app.revanced.api.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

internal fun Route.rootRoute() {
    val apiService = get<ApiService>()
    val authService = get<AuthService>()

    get("contributors") {
        call.respond(apiService.contributors())
    }

    get("team") {
        call.respond(apiService.team())
    }

    route("ping") {
        handle {
            call.respond(HttpStatusCode.NoContent)
        }
    }

    authenticate("basic") {
        get("token") {
            call.respond(authService.newToken())
        }
    }

    staticResources("/", "/app/revanced/api/static") {
        contentType { ContentType.Application.Json }
        extensions("json")
    }
}
