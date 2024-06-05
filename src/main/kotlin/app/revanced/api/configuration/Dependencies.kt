package app.revanced.api.configuration

import app.revanced.api.repository.AnnouncementRepository
import app.revanced.api.repository.ConfigurationRepository
import app.revanced.api.repository.backend.BackendRepository
import app.revanced.api.repository.backend.github.GitHubBackendRepository
import app.revanced.api.services.AnnouncementService
import app.revanced.api.services.ApiService
import app.revanced.api.services.AuthService
import app.revanced.api.services.PatchesService
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

fun Application.configureDependencies() {
    val globalModule = module {
        single {
            Dotenv.configure()
                .systemProperties()
                .load()
        }
    }

    val repositoryModule = module {
        single {
            val dotenv = get<Dotenv>()

            Database.connect(
                url = dotenv["DB_URL"],
                user = dotenv["DB_USER"],
                password = dotenv["DB_PASSWORD"],
                driver = "org.h2.Driver",
            )
        }

        single {
            val configFilePath = get<Dotenv>()["CONFIG_FILE_PATH"]
            val configFile = File(configFilePath).inputStream()

            Toml.decodeFromStream<ConfigurationRepository>(configFile)
        }

        singleOf(::AnnouncementRepository)
    }

    val serviceModule = module {
        single {
            val dotenv = get<Dotenv>()

            val jwtSecret = dotenv["JWT_SECRET"]
            val issuer = dotenv["JWT_ISSUER"]
            val validityInMin = dotenv["JWT_VALIDITY_IN_MIN"].toInt()

            val basicUsername = dotenv["BASIC_USERNAME"]
            val basicPassword = dotenv["BASIC_PASSWORD"]

            AuthService(issuer, validityInMin, jwtSecret, basicUsername, basicPassword)
        }
        single {
            val token = get<Dotenv>()["GITHUB_TOKEN"]

            GitHubBackendRepository(token)
        } bind BackendRepository::class
        singleOf(::AnnouncementService)
        singleOf(::PatchesService)
        singleOf(::ApiService)
    }

    install(Koin) {
        modules(
            globalModule,
            repositoryModule,
            serviceModule,
        )
    }
}
