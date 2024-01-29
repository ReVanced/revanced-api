package app.revanced.api.plugins

import app.revanced.api.backend.github.GitHubBackend
import app.revanced.api.schema.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val backend by inject<GitHubBackend>()
    val configuration by inject<APIConfiguration>()

    routing {
        route("/v${configuration.apiVersion}") {
            route("/patches") {
                route("latest") {
                    get {
                        val patches = backend.getRelease(configuration.organization, configuration.patchesRepository)
                        val integrations = configuration.integrationsRepositoryNames.map {
                            async { backend.getRelease(configuration.organization, it) }
                        }.awaitAll()

                        val assets = (patches.assets + integrations.flatMap { it.assets }).filter {
                            it.downloadUrl.endsWith(".apk") || it.downloadUrl.endsWith(".jar")
                        }.map { APIAsset(it.downloadUrl) }.toSet()

                        val release = APIRelease(
                            patches.tag,
                            patches.createdAt,
                            patches.releaseNote,
                            assets
                        )

                        call.respond(release)
                    }

                    get("/version") {
                        val patches = backend.getRelease(configuration.organization, configuration.patchesRepository)

                        val release = APIReleaseVersion(patches.tag)

                        call.respond(release)
                    }
                }
            }

            get("/contributors") {
                val contributors = configuration.contributorsRepositoryNames.map {
                    async {
                        APIContributable(
                            it,
                            backend.getContributors(configuration.organization, it).map {
                                APIContributor(it.name, it.avatarUrl, it.url, it.contributions)
                            }.toSet()
                        )
                    }
                }.awaitAll()

                call.respond(contributors)
            }

            get("/members") {
                val members = backend.getMembers(configuration.organization).map {
                    APIMember(it.name, it.avatarUrl, it.url, it.gpgKeysUrl)
                }

                call.respond(members)
            }

            route("/ping") {
                handle {
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            staticResources("/", "/static/api") {
                contentType { ContentType.Application.Json }
                extensions("json")
            }
        }

    }
}
