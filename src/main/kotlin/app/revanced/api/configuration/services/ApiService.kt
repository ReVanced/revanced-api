package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

internal class ApiService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    val versionedStaticFilesPath = configurationRepository.versionedStaticFilesPath
    val about = configurationRepository.about

    suspend fun contributors() = withContext(Dispatchers.IO) {
        configurationRepository.contributorsRepositoryNames.map { (repository, name) ->
            async {
                APIContributable(
                    name,
                    URLBuilder().apply {
                        takeFrom(backendRepository.website)
                        path(configurationRepository.organization, repository)
                    }.buildString(),
                    backendRepository.contributors(configurationRepository.organization, repository).map {
                        ApiContributor(it.name, it.avatarUrl, it.url, it.contributions)
                    },
                )
            }
        }
    }.awaitAll()

    suspend fun team() = backendRepository.members(configurationRepository.organization).map { member ->
        ApiMember(
            member.name,
            member.avatarUrl,
            member.url,
            member.bio,
            if (member.gpgKeys.ids.isNotEmpty()) {
                ApiGpgKey(
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
        ApiRateLimit(it.limit, it.remaining, it.reset)
    }
}
