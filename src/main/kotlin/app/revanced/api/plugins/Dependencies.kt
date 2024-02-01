package app.revanced.api.plugins

import app.revanced.api.backend.github.GitHubBackend
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun Application.configureDependencies() {

    install(Koin) {
        modules(
            module {
                single { Dotenv.load() }
                single { GitHubBackend(get<Dotenv>().get("GITHUB_TOKEN")) }
            }
        )
    }
}
