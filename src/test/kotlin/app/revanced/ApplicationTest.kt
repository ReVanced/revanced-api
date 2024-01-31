package app.revanced

import app.revanced.api.modules.*
import app.revanced.api.schema.APIConfiguration
import com.akuleshov7.ktoml.Toml
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `successfully create a token`() = testApplication {
        val apiConfigurationFile = kotlin.io.path.createTempFile().toFile().apply {
            Toml.encodeToString(
                APIConfiguration(
                    organization = "ReVanced",
                    patchesRepository = "",
                    integrationsRepositoryNames = setOf(),
                    contributorsRepositoryNames = setOf()
                )
            ).let(::writeText)

            deleteOnExit()
        }

        val dotenv = mockk<Dotenv>()
        every { dotenv[any()] } returns "ReVanced"
        every { dotenv["JWT_VALIDITY_IN_MIN"] } returns "5"
        every { dotenv["CONFIG_FILE_PATH"] } returns apiConfigurationFile.absolutePath

        application {
            configureDependencies()
            configureHTTP()
            configureSerialization()
            configureSecurity()
            configureRouting()
        }

        val token = client.get("/v1/token") {
            headers {
                append(
                    HttpHeaders.Authorization,
                    "Basic ${"${dotenv["BASIC_USERNAME"]}:${dotenv["BASIC_PASSWORD"]}".encodeBase64()}"
                )
            }
        }.bodyAsText()

        assert(token.isNotEmpty())
    }
}
