package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.ApiRelease
import app.revanced.api.configuration.schema.ApiReleaseVersion

internal class ManagerService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun latestRelease(): ApiRelease {
        val managerRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
        )

        return ApiRelease(
            managerRelease.tag,
            managerRelease.createdAt,
            managerRelease.releaseNote,
            managerRelease.assets.first(configurationRepository.manager.assetRegex).downloadUrl,
        )
    }

    suspend fun latestVersion(): ApiReleaseVersion {
        val managerRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
        )

        return ApiReleaseVersion(managerRelease.tag)
    }
}
