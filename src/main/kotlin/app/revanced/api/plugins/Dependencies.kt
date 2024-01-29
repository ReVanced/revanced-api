package app.revanced.api.plugins

import app.revanced.api.schema.APIConfiguration
import app.revanced.api.backend.github.GitHubBackend
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

fun Application.configureDependencies() {

    install(Koin) {
        modules(
            module {
                single {
                    Dotenv.load()
                }
                single {
                    val configFilePath = get<Dotenv>().get("CONFIG_FILE_PATH")!!
                    Toml.decodeFromStream<APIConfiguration>(File(configFilePath).inputStream())
                }
                single {
                    val token = get<Dotenv>().get("GITHUB_TOKEN")
                    GitHubBackend(token)
                }
            }
        )
    }
}

