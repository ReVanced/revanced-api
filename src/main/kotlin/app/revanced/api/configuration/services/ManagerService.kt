package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.*

internal class ManagerService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun latestRelease(): ApiRelease<ApiManagerAsset> {
        val managerRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
        )

        val managerAsset = ApiManagerAsset(
            managerRelease.assets.first(configurationRepository.manager.assetRegex).downloadUrl,
        )

        return ApiRelease(
            managerRelease.tag,
            managerRelease.createdAt,
            managerRelease.releaseNote,
            listOf(managerAsset),
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
