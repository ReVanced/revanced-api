package app.revanced.api.configuration

import app.revanced.api.command.applicationVersion
import app.revanced.api.configuration.repository.ConfigurationRepository
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource
import org.koin.ktor.ext.get as koinGet

internal fun Application.configureOpenAPI() {
    routing {
        swaggerUI(path = "/") {
            info = OpenApiInfo(
                title = "ReVanced API",
                version = applicationVersion,
                description = "API server for ReVanced.",
                contact = OpenApiInfo.Contact(
                    name = "ReVanced",
                    url = "https://revanced.app",
                    email = "contact@revanced.app",
                ),
                license = OpenApiInfo.License(
                    name = "AGPLv3",
                    url = "https://github.com/ReVanced/revanced-api/blob/main/LICENSE",
                ),
            )
            source = OpenApiDocSource.Routing { routingRoot.descendants() }
        }
    }
}
