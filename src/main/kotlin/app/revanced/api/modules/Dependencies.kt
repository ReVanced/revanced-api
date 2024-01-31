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
        val configFilePath = get<Dotenv>().get("CONFIG_FILE_PATH")!!
        Toml.decodeFromStream<APIConfiguration>(File(configFilePath).inputStream())
    }
}

val gitHubBackendModule = module {
    single {
        val token = get<Dotenv>().get("GITHUB_TOKEN")
        GitHubBackend(token)
    } bind Backend::class
}

val databaseModule = module {
    single {
        val dotenv = get<Dotenv>()

        Database.connect(
            url = dotenv.get("DB_URL"),
            user = dotenv.get("DB_USER"),
            password = dotenv.get("DB_PASSWORD"),
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

        val jwtSecret = dotenv.get("JWT_SECRET")!!
        val issuer = dotenv.get("JWT_ISSUER")!!
        val validityInMin = dotenv.get("JWT_VALIDITY_IN_MIN")!!.toInt()

        val basicUsername = dotenv.get("BASIC_USERNAME")!!
        val basicPassword = dotenv.get("BASIC_PASSWORD")!!

        AuthService(issuer, validityInMin, jwtSecret, basicUsername, basicPassword)
    }
}
