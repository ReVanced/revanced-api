package app.revanced.api.command

import picocli.CommandLine
import java.util.*

internal val applicationVersion = MainCommand::class.java.getResourceAsStream(
    "/app/revanced/api/version.properties",
)?.use { stream ->
    Properties().apply {
        load(stream)
    }.getProperty("version")
} ?: "v0.0.0"

fun main(args: Array<String>) {
    CommandLine(MainCommand).execute(*args).let(System::exit)
}

private object CLIVersionProvider : CommandLine.IVersionProvider {
    override fun getVersion() =
        arrayOf(
            "ReVanced API $applicationVersion",
        )
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
