package app.revanced.api.command

import app.revanced.api.plugins.*
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
    )
    private var host: String = "0.0.0.0"

    @CommandLine.Option(
        names = ["-p", "--port"],
        description = ["The port to listen on."],
    )
    private var port: Int = 8080

    override fun run() {
        embeddedServer(Netty, port, host, configure = {
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
}
