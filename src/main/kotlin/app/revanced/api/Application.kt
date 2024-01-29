package app.revanced.api

import app.revanced.api.plugins.*
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", configure = {
        connectionGroupSize = 1
        workerGroupSize = 1
        callGroupSize = 1
    }) {
        configureHTTP()
        configureSerialization()
        configureDatabases()
        configureSecurity()
        configureDependencies()
        configureRouting()
    }.start(wait = true)
}
