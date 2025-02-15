package app.revanced.api.configuration

import app.revanced.api.command.applicationVersion
import app.revanced.api.configuration.repository.ConfigurationRepository
import io.bkbn.kompendium.core.attribute.KompendiumAttributes
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Contact
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.info.License
import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.server.Server
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URI
import org.koin.ktor.ext.get as koinGet

internal fun Application.configureOpenAPI() {
    val configuration = koinGet<ConfigurationRepository>()

    install(NotarizedApplication()) {
        openApiJson = {
            route("/${configuration.apiVersion}/openapi.json") {
                get {
                    call.respond(application.attributes[KompendiumAttributes.openApiSpec])
                }
            }
        }
        spec = OpenApiSpec(
            info = Info(
                title = "ReVanced API",
                version = applicationVersion,
                description = "API server for ReVanced.",
                contact = Contact(
                    name = "ReVanced",
                    url = URI("https://revanced.app"),
                    email = "contact@revanced.app",
                ),
                license = License(
                    name = "AGPLv3",
                    url = URI("https://github.com/ReVanced/revanced-api/blob/main/LICENSE"),
                ),
            ),
            components = Components(
                securitySchemes = mutableMapOf(
                    "bearer" to BearerAuth(),
                ),
            ),
        ).apply {
            servers += Server(
                url = URI(configuration.endpoint),
                description = "ReVanced API server",
            )
        }

        schemaConfigurator = KotlinXSchemaConfigurator()
    }
}
