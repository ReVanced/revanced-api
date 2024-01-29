package app.revanced.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class APIConfiguration(
    val organization: String,
    @SerialName("patches-repository")
    val patchesRepository: String,
    @SerialName("integrations-repositories")
    val integrationsRepositoryNames: Set<String>,
    @SerialName("contributors-repositories")
    val contributorsRepositoryNames: Set<String>,
    @SerialName("api-version")
    val apiVersion: Int = 1
)