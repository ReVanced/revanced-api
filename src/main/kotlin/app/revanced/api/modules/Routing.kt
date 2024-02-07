package app.revanced.api.modules

import app.revanced.api.backend.Backend
import app.revanced.api.schema.*
import app.revanced.library.PatchUtils
import app.revanced.patcher.PatchBundleLoader
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
import java.net.URL
import org.koin.ktor.ext.get as koinGet

fun Application.configureRouting() {
    val backend: Backend = koinGet()
    val configuration: APIConfiguration = koinGet()
    val announcementService: AnnouncementService = koinGet()
    val authService: AuthService = koinGet()

    routing {
        route("/v${configuration.apiVersion}") {
            route("/announcements") {
                suspend fun PipelineContext<*, ApplicationCall>.announcement(block: AnnouncementService.() -> APIResponseAnnouncement?) =
                    announcementService.block()?.let { call.respond(it) }
                        ?: call.respond(HttpStatusCode.NotFound)

                suspend fun PipelineContext<*, ApplicationCall>.announcementId(block: AnnouncementService.() -> APILatestAnnouncement?) =
                    announcementService.block()?.let { call.respond(it) }
                        ?: call.respond(HttpStatusCode.NotFound)

                suspend fun PipelineContext<*, ApplicationCall>.channel(block: suspend (String) -> Unit) =
                    block(call.parameters["channel"]!!)

                route("/{channel}/latest") {
                    get("/id") {
                        channel {
                            announcementId {
                                latestId(it)
                            }
                        }
                    }

                    get {
                        channel {
                            announcement {
                                latest(it)
                            }
                        }
                    }
                }

                get("/{channel}") {
                    channel {
                        call.respond(announcementService.read(it))
                    }
                }

                route("/latest") {
                    get("/id") {
                        announcementId {
                            latestId()
                        }
                    }

                    get {
                        announcement {
                            latest()
                        }
                    }
                }

                get {
                    call.respond(announcementService.read())
                }

                authenticate("jwt") {
                    suspend fun PipelineContext<*, ApplicationCall>.id(block: suspend (Int) -> Unit) =
                        call.parameters["id"]!!.toIntOrNull()?.let {
                            block(it)
                        } ?: call.respond(HttpStatusCode.BadRequest)

                    post {
                        announcementService.new(call.receive<APIAnnouncement>())
                    }

                    post("/{id}/archive") {
                        id {
                            val archivedAt = call.receiveNullable<APIAnnouncementArchivedAt>()?.archivedAt
                            announcementService.archive(it, archivedAt)
                        }
                    }

                    post("/{id}/unarchive") {
                        id {
                            announcementService.unarchive(it)
                        }
                    }

                    patch("/{id}") {
                        id {
                            announcementService.update(it, call.receive<APIAnnouncement>())
                        }
                    }

                    delete("/{id}") {
                        id {
                            announcementService.delete(it)
                        }
                    }
                }
            }

            route("/patches") {
                route("latest") {
                    get {
                        val patchesRelease =
                            backend.getRelease(configuration.organization, configuration.patchesRepository)
                        val integrationsReleases = configuration.integrationsRepositoryNames.map {
                            async { backend.getRelease(configuration.organization, it) }
                        }.awaitAll()

                        val assets = (patchesRelease.assets + integrationsReleases.flatMap { it.assets })
                            .map { APIAsset(it.downloadUrl) }
                            .filter { it.type != APIAsset.Type.UNKNOWN }
                            .toSet()

                        val apiRelease = APIRelease(
                            patchesRelease.tag,
                            patchesRelease.createdAt,
                            patchesRelease.releaseNote,
                            assets,
                        )

                        call.respond(apiRelease)
                    }

                    get("/version") {
                        val patchesRelease =
                            backend.getRelease(configuration.organization, configuration.patchesRepository)

                        val apiPatchesRelease = APIReleaseVersion(patchesRelease.tag)

                        call.respond(apiPatchesRelease)
                    }

                    val fileCache = Caffeine
                        .newBuilder()
                        .evictionListener<String, File> { _, value, _ -> value?.delete() }
                        .maximumSize(1)
                        .build<String, File>()

                    get("/list") {
                        val patchesRelease =
                            backend.getRelease(configuration.organization, configuration.patchesRepository)

                        // Get the cached patches file or download and cache a new one.
                        // The old file is deleted on eviction.
                        val patchesFile = fileCache.getIfPresent(patchesRelease.tag) ?: run {
                            val downloadUrl = patchesRelease.assets
                                .map { APIAsset(it.downloadUrl) }
                                .find { it.type == APIAsset.Type.PATCHES }
                                ?.downloadUrl

                            kotlin.io.path.createTempFile().toFile().apply {
                                outputStream().use { URL(downloadUrl).openStream().copyTo(it) }
                            }.also {
                                fileCache.put(patchesRelease.tag, it)
                                it.deleteOnExit()
                            }
                        }

                        call.respondOutputStream(
                            contentType = ContentType.Application.Json,
                        ) {
                            PatchUtils.Json.serialize(
                                PatchBundleLoader.Jar(patchesFile),
                                outputStream = this,
                            )
                        }
                    }
                }
            }

            staticResources("/", "/static/api") {
                contentType { ContentType.Application.Json }
                extensions("json")
            }

            get("/contributors") {
                val contributors =
                    configuration.contributorsRepositoryNames.map {
                        async {
                            APIContributable(
                                it,
                                backend.getContributors(configuration.organization, it).map {
                                    APIContributor(it.name, it.avatarUrl, it.url, it.contributions)
                                }.toSet(),
                            )
                        }
                    }.awaitAll()

                call.respond(contributors)
            }

            get("/team") {
                val team =
                    backend.getMembers(configuration.organization).map {
                        APIMember(it.name, it.avatarUrl, it.url, it.gpgKeysUrl)
                    }

                call.respond(team)
            }

            route("/ping") {
                handle {
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            authenticate("basic") {
                get("/token") {
                    call.respond(authService.newToken())
                }
            }
        }
    }
}
