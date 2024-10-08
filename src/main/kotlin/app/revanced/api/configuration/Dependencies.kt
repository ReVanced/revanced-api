package app.revanced.api.configuration

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.repository.GitHubBackendRepository
import app.revanced.api.configuration.services.*
import app.revanced.api.configuration.services.AnnouncementService
import app.revanced.api.configuration.services.ApiService
import app.revanced.api.configuration.services.AuthenticationService
import app.revanced.api.configuration.services.OldApiService
import app.revanced.api.configuration.services.PatchesService
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parameterArrayOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureDependencies(
    configFile: File,
) {
    val miscModule = module {
        factory { params ->
            val defaultRequestUri: String = params.get<String>()
            val configBlock = params.getOrNull<(HttpClientConfig<OkHttpConfig>.() -> Unit)>() ?: {}

            HttpClient(OkHttp) {
                defaultRequest { url(defaultRequestUri) }

                configBlock()
            }
        }
    }

    val repositoryModule = module {
        single<BackendRepository> {
            GitHubBackendRepository(
                get {
                    val defaultRequestUri = "https://api.github.com"
                    val configBlock: HttpClientConfig<OkHttpConfig>.() -> Unit = {
                        install(HttpCache)
                        install(Resources)
                        install(ContentNegotiation) {
                            json(
                                Json {
                                    ignoreUnknownKeys = true
                                    namingStrategy = JsonNamingStrategy.SnakeCase
                                },
                            )
                        }

                        System.getProperty("BACKEND_API_TOKEN")?.let {
                            install(Auth) {
                                bearer {
                                    loadTokens {
                                        BearerTokens(
                                            accessToken = it,
                                            refreshToken = "", // Required dummy value
                                        )
                                    }

                                    sendWithoutRequest { true }
                                }
                            }
                        }
                    }

                    parameterArrayOf(defaultRequestUri, configBlock)
                },
            )
        }

        single<ConfigurationRepository> {
            Toml.decodeFromStream(configFile.inputStream())
        }

        single {
            TransactionManager.defaultDatabase = Database.connect(
                url = System.getProperty("DB_URL"),
                user = System.getProperty("DB_USER"),
                password = System.getProperty("DB_PASSWORD"),
            )

            AnnouncementRepository()
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
        single {
            val configuration = get<ConfigurationRepository>()

            OldApiService(
                get {
                    parameterArrayOf(configuration.oldApiEndpoint)
                },
            )
        }
        singleOf(::AnnouncementService)
        singleOf(::SignatureService)
        singleOf(::PatchesService)
        singleOf(::ManagerService)
        singleOf(::ApiService)
    }

    install(Koin) {
        modules(
            miscModule,
            repositoryModule,
            serviceModule,
        )
    }
}
