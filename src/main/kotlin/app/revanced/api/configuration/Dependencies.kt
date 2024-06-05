package app.revanced.api.configuration

import app.revanced.api.repository.AnnouncementRepository
import app.revanced.api.repository.ConfigurationRepository
import app.revanced.api.repository.OldApiService
import app.revanced.api.repository.backend.BackendRepository
import app.revanced.api.repository.backend.github.GitHubBackendRepository
import app.revanced.api.services.AnnouncementService
import app.revanced.api.services.ApiService
import app.revanced.api.services.AuthService
import app.revanced.api.services.PatchesService
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.github.cdimascio.dotenv.Dotenv
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
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parameterArrayOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureDependencies() {
    val globalModule = module {
        single {
            Dotenv.configure().load()
        }
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
        single {
            val dotenv = get<Dotenv>()

            Database.connect(
                url = dotenv["DB_URL"],
                user = dotenv["DB_USER"],
                password = dotenv["DB_PASSWORD"],
                driver = "org.h2.Driver",
            )
        }

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

                        get<Dotenv>()["BACKEND_API_TOKEN"]?.let {
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
            OldApiService(
                get {
                    val defaultRequestUri = get<Dotenv>()["OLD_API_URL"]
                    parameterArrayOf(defaultRequestUri)
                },
            )
        }
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
