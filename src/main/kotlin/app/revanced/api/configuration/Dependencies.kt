package app.revanced.api.configuration

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.repository.GitHubBackendRepository
import app.revanced.api.configuration.services.*
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

fun Application.configureDependencies(
    configFile: File,
) {
    val repositoryModule = module {
        single<ConfigurationRepository> { Toml.decodeFromStream(configFile.inputStream()) }
        single {
            Database.connect(
                url = System.getProperty("DB_URL"),
                user = System.getProperty("DB_USER"),
                password = System.getProperty("DB_PASSWORD"),
            )
        }
        singleOf(::AnnouncementRepository)
        singleOf(::GitHubBackendRepository)
        single<BackendRepository> {
            val backendServices = mapOf(
                GitHubBackendRepository.SERVICE_NAME to { get<GitHubBackendRepository>() },
                // Implement more backend services here.
            )

            val configuration = get<ConfigurationRepository>()
            val backendFactory = backendServices[configuration.backendServiceName]!!

            backendFactory()
        }
    }

    val serviceModule = module {
        single {
            val jwtSecret = System.getProperty("JWT_SECRET")
            val issuer = System.getProperty("JWT_ISSUER")
            val validityInMin = System.getProperty("JWT_VALIDITY_IN_MIN").toLong()

            val authSHA256DigestString = System.getProperty("AUTH_SHA256_DIGEST")

            AuthenticationService(issuer, validityInMin, jwtSecret, authSHA256DigestString)
        }
        singleOf(::AnnouncementService)
        singleOf(::SignatureService)
        singleOf(::PatchesService)
        singleOf(::ManagerService)
        singleOf(::ApiService)
    }

    install(Koin) {
        modules(
            repositoryModule,
            serviceModule,
        )
    }
}
