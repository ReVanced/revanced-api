package app.revanced.api.command

import app.revanced.api.configuration.*
import app.revanced.api.configuration.routing.configureRouting
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import picocli.CommandLine

@CommandLine.Command(
    name = "start",
    description = ["Start the API server"],
)
internal object StartAPICommand : Runnable {
    @CommandLine.Option(
        names = ["-h", "--host"],
        description = ["The host address to bind to."],
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
    )
    private var host: String = "0.0.0.0"

    @CommandLine.Option(
        names = ["-p", "--port"],
        description = ["The port to listen on."],
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
    )
    private var port: Int = 8080

    override fun run() {
        embeddedServer(Netty, port, host) {
            configureDependencies()
            configureHTTP(allowedHost = host)
            configureSerialization()
            configureSecurity()
            configureRouting()
        }.start(wait = true)
    }
}
