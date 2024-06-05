package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.repository.backend.BackendRepository
import app.revanced.api.configuration.schema.APIContributable
import app.revanced.api.configuration.schema.APIContributor
import app.revanced.api.configuration.schema.APIMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

internal class ApiService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun contributors() = withContext(Dispatchers.IO) {
        configurationRepository.contributorsRepositoryNames.map {
            async {
                APIContributable(
                    it,
                    backendRepository.contributors(configurationRepository.organization, it).map {
                        APIContributor(it.name, it.avatarUrl, it.url, it.contributions)
                    }.toSet(),
                )
            }
        }
    }.awaitAll()

    suspend fun team() = backendRepository.members(configurationRepository.organization).map {
        APIMember(it.name, it.avatarUrl, it.url, it.gpgKeysUrl)
    }
}
