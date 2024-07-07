package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.*
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
                    },
                )
            }
        }
    }.awaitAll()

    suspend fun team() = backendRepository.members(configurationRepository.organization).map { member ->
        APIMember(
            member.name,
            member.avatarUrl,
            member.url,
            if (member.gpgKeys.ids.isNotEmpty()) {
                APIGpgKey(
                    // Must choose one of the GPG keys, because it does not make sense to have multiple GPG keys for the API.
                    member.gpgKeys.ids.first(),
                    member.gpgKeys.url,
                )
            } else {
                null
            },

        )
    }

    suspend fun rateLimit() = backendRepository.rateLimit()?.let {
        APIRateLimit(it.limit, it.remaining, it.reset)
    }
}
