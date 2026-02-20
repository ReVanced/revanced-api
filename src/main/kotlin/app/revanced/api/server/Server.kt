package app.revanced.api.server

import app.revanced.api.applicationVersion
import app.revanced.api.server.repositories.AnnouncementRepository
import app.revanced.api.server.repositories.BackendRepository
import app.revanced.api.server.repositories.ConfigurationRepository
import app.revanced.api.server.repositories.GitHubBackendRepository
import app.revanced.api.server.routes.announcementsRoute
import app.revanced.api.server.routes.apiRoute
import app.revanced.api.server.routes.managerRoute
import app.revanced.api.server.routes.patchesRoute
import app.revanced.api.server.services.AnnouncementService
import app.revanced.api.server.services.ApiService
import app.revanced.api.server.services.AuthenticationService
import app.revanced.api.server.services.ManagerService
import app.revanced.api.server.services.PatchesService
import app.revanced.api.server.services.SignatureService
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.StaticContentConfig
import io.ktor.server.http.content.staticFiles
import io.ktor.server.jetty.jakarta.Jetty
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.RateLimitProviderConfig
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun startServer(configFile: File, host: String, port: Int) {
    Dotenv.configure().systemProperties().load()

    embeddedServer(Jetty, port, host) {
        configureDependencies(configFile)
        configureHTTP()
        configureSerialization()
        configureSecurity()
        configureOpenAPI()
        configureLogging()
        configureRouting()
    }.start(wait = true)
}


internal fun Application.configureOpenAPI() {
    routing {
        swaggerUI(path = "/") {
            info = OpenApiInfo(
                title = "ReVanced API",
                version = applicationVersion,
                description = "API server for ReVanced.",
                contact = OpenApiInfo.Contact(
                    name = "ReVanced",
                    url = "https://revanced.app",
                    email = "contact@revanced.app",
                ),
                license = OpenApiInfo.License(
                    name = "AGPLv3",
                    url = "https://github.com/ReVanced/revanced-api/blob/main/LICENSE",
                ),
            )
            source = OpenApiDocSource.Routing { routingRoot.descendants() }
        }
    }
}

internal fun Application.configureRouting() = routing {
    val configuration = get<ConfigurationRepository>()

    installCache(5.minutes)

    route("/${configuration.apiVersion}") {
        announcementsRoute()
        patchesRoute()
        managerRoute()
        apiRoute()
    }

    staticFiles("/", configuration.staticFilesPath) {
        contentType {
            when (it.extension) {
                "json" -> ContentType.Application.Json
                "asc" -> ContentType.Text.Plain
                "ico" -> ContentType.Image.XIcon
                "svg" -> ContentType.Image.SVG
                "jpg", "jpeg" -> ContentType.Image.JPEG
                "png" -> ContentType.Image.PNG
                "gif" -> ContentType.Image.GIF
                "mp4" -> ContentType.Video.MP4
                "ogg" -> ContentType.Video.OGG
                "mp3" -> ContentType.Audio.MPEG
                "css" -> ContentType.Text.CSS
                "js" -> ContentType.Application.JavaScript
                "html" -> ContentType.Text.Html
                "xml" -> ContentType.Application.Xml
                "pdf" -> ContentType.Application.Pdf
                "zip" -> ContentType.Application.Zip
                "gz" -> ContentType.Application.GZip
                else -> ContentType.Application.OctetStream
            }
        }

        extensions("json", "asc")
    }
}

fun Application.configureSecurity() {
    val authenticationService = get<AuthenticationService>()

    install(Authentication) {
        with(authenticationService) {
            jwt()
            digest()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                namingStrategy = JsonNamingStrategy.SnakeCase
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }
}

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

internal suspend fun ApplicationCall.respondOrNotFound(value: Any?) =
    respond(value ?: HttpStatusCode.NotFound)

internal fun Route.installCache(maxAge: Duration) =
    installCache(CacheControl.MaxAge(maxAgeSeconds = maxAge.inWholeSeconds.toInt()))

internal fun Route.installNoCache() = installCache(CacheControl.NoCache(null))

internal fun Route.installCache(cacheControl: CacheControl) = install(CachingHeaders) {
    options { _, _ ->
        CachingOptions(cacheControl)
    }
}

internal fun Route.staticFiles(
    remotePath: String,
    dir: Path,
    block: StaticContentConfig<File>.() -> Unit = {
        contentType {
            ContentType.Application.Json
        }
        extensions("json")
    },
) = staticFiles(remotePath, dir.toFile(), null, block)

fun Application.configureHTTP() {
    val configuration = get<ConfigurationRepository>()

    install(CORS) {
        HttpMethod.DefaultMethods.minus(HttpMethod.Options).forEach(::allowMethod)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        exposeHeader(HttpHeaders.WWWAuthenticate)

        allowCredentials = true

        configuration.corsAllowedHosts.forEach { host ->
            allowHost(host = host, schemes = listOf("https"))
        }
    }

    install(RateLimit) {
        fun rateLimit(name: String, block: RateLimitProviderConfig.() -> Unit) =
            register(RateLimitName(name)) {
                requestKey {
                    it.request.uri + it.request.origin.remoteAddress
                }

                block()
            }

        rateLimit("weak") {
            rateLimiter(limit = 30, refillPeriod = 2.minutes)
        }
        rateLimit("strong") {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
        }
    }
}

internal fun Application.configureLogging() {
    install(CallLogging) {
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            "$status $httpMethod $uri"
        }
    }
}
