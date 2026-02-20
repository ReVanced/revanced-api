package app.revanced.api

import app.revanced.api.server.startServer
import picocli.CommandLine
import java.io.File
import java.util.*
import kotlin.arrayOf

internal val applicationVersion = MainCommand::class.java.getResourceAsStream(
    "/app/revanced/api/version.properties",
)?.use { stream ->
    Properties().apply { load(stream) }.getProperty("version")
} ?: "v0.0.0"

fun main(args: Array<String>) = CommandLine(MainCommand).execute(*args).let(System::exit)

private object CLIVersionProvider : CommandLine.IVersionProvider {
    override fun getVersion() = arrayOf("ReVanced API $applicationVersion")
}

@CommandLine.Command(
    name = "revanced-api",
    description = ["API server for ReVanced"],
    mixinStandardHelpOptions = true,
    versionProvider = CLIVersionProvider::class,
    subcommands = [
        StartAPICommand::class,
    ],
)

private object MainCommand

@CommandLine.Command(
    name = "start",
    description = ["Start the API server"],
)
private object StartAPICommand : Runnable {
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

    override fun run() = startServer(configFile, host, port)
}
