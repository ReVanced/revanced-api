package app.revanced.api.command

import picocli.CommandLine
import java.util.*

fun main(args: Array<String>) {
    CommandLine(MainCommand).execute(*args).let(System::exit)
}

private object CLIVersionProvider : CommandLine.IVersionProvider {
    override fun getVersion() =
        arrayOf(
            MainCommand::class.java.getResourceAsStream(
                "/app/revanced/api/version.properties",
            )?.use { stream ->
                Properties().apply {
                    load(stream)
                }.let {
                    "ReVanced API v${it.getProperty("version")}"
                }
            } ?: "ReVanced API",
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
