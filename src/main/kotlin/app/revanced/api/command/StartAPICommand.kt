package app.revanced.api.command

import app.revanced.api.configuration.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import picocli.CommandLine
import java.io.File

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
    private var port: Int = 8888

    @CommandLine.Option(
        names = ["-c", "--config"],
        description = ["The path to the configuration file."],
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
    )
    private var configFile = File("configuration.toml")

    override fun run() {
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
}
