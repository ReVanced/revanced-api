package app.revanced.api.services

import app.revanced.api.repository.ConfigurationRepository
import app.revanced.api.repository.backend.BackendRepository
import app.revanced.api.schema.APIContributable
import app.revanced.api.schema.APIContributor
import app.revanced.api.schema.APIMember
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
