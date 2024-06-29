package app.revanced.api.configuration

import app.revanced.api.command.applicationVersion
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Contact
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.info.License
import io.bkbn.kompendium.oas.security.BasicAuth
import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.server.Server
import io.ktor.server.application.*
import java.net.URI

internal fun Application.configureOpenAPI() {
    install(NotarizedApplication()) {
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
                    "basic" to BasicAuth(),
                ),
            ),
        ).apply {
            servers += Server(
                url = URI("https://api.revanced.app"),
                description = "ReVanced API server",
            )
        }

        schemaConfigurator = KotlinXSchemaConfigurator()
    }
}
