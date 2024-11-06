package app.revanced.api.configuration

import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.routes.*
import app.revanced.api.configuration.routes.announcementsRoute
import app.revanced.api.configuration.routes.apiRoute
import app.revanced.api.configuration.routes.patchesRoute
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.core.routes.swagger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes
import org.koin.ktor.ext.get as koinGet

internal fun Application.configureRouting() = routing {
    val configuration = koinGet<ConfigurationRepository>()

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

    swagger(pageTitle = "ReVanced API", path = "/")
    redoc(pageTitle = "ReVanced API", path = "/redoc")
}
