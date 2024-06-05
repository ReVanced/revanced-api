package app.revanced.api.configuration.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class ConfigurationRepository(
    val organization: String,
    @SerialName("patches-repository")
    val patchesRepository: String,
    @SerialName("integrations-repositories")
    val integrationsRepositoryNames: Set<String>,
    @SerialName("contributors-repositories")
    val contributorsRepositoryNames: Set<String>,
    @SerialName("api-version")
    val apiVersion: Int = 1,
)
