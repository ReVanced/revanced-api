package app.revanced.api.configuration

import app.revanced.api.configuration.services.AuthService
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun Application.configureSecurity() {
    get<AuthService>().configureSecurity(this)
}
