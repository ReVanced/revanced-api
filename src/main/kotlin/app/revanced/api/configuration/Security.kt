package app.revanced.api.configuration

import app.revanced.api.configuration.services.AuthenticationService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.koin.ktor.ext.get

fun Application.configureSecurity() {
    val authenticationService = get<AuthenticationService>()

    install(Authentication) {
        with(authenticationService) {
            jwt()
            digest()
        }
    }
}
