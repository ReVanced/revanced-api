package app.revanced.api.modules

import app.revanced.api.backend.Backend
import app.revanced.api.backend.github.GitHubBackend
import app.revanced.api.schema.APIConfiguration
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

fun Application.configureDependencies() {
    install(Koin) {
        modules(
            globalModule,
            gitHubBackendModule,
            databaseModule,
            authModule
        )
    }
}

val globalModule = module {
    single {
        Dotenv.load()
    }
    single {
        val configFilePath = get<Dotenv>()["CONFIG_FILE_PATH"]
        Toml.decodeFromStream<APIConfiguration>(File(configFilePath).inputStream())
    }
}

val gitHubBackendModule = module {
    single {
        val token = get<Dotenv>()["GITHUB_TOKEN"]
        GitHubBackend(token)
    } bind Backend::class
}

val databaseModule = module {
    single {
        val dotenv = get<Dotenv>()

        Database.connect(
            url = dotenv["DB_URL"],
            user = dotenv["DB_USER"],
            password = dotenv["DB_PASSWORD"],
            driver = "org.h2.Driver"
        )
    }
    factory<AnnouncementService> {
        AnnouncementService(get())
    }
}

val authModule = module {
    single {
        val dotenv = get<Dotenv>()

        val jwtSecret = dotenv["JWT_SECRET"]
        val issuer = dotenv["JWT_ISSUER"]
        val validityInMin = dotenv["JWT_VALIDITY_IN_MIN"].toInt()

        val basicUsername = dotenv["BASIC_USERNAME"]
        val basicPassword = dotenv["BASIC_PASSWORD"]

        AuthService(issuer, validityInMin, jwtSecret, basicUsername, basicPassword)
    }
}
