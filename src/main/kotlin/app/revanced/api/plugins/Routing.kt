package app.revanced.api.plugins

import app.revanced.api.backend.github.GitHubBackend
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val backend by inject<GitHubBackend>()
    val dotenv by inject<Dotenv>()

    routing {
        route("/v${dotenv.get("API_VERSION", "1")}") {
            route("/manager") {
                get("/contributors") {
                    val contributors = backend.getContributors("revanced", "revanced-patches")

                    call.respond(contributors)
                }

                get("/members") {
                    val members = backend.getMembers("revanced")

                    call.respond(members)
                }
            }

            route("/patches") {

            }

            route("/ping") {
                handle {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        staticResources("/", "static")
    }
}
