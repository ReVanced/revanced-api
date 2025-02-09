package app.revanced.api.configuration.services

import app.revanced.api.configuration.ApiRelease
import app.revanced.api.configuration.ApiReleaseVersion
import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.configuration.repository.ConfigurationRepository

internal class ManagerService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun latestRelease(prerelease: Boolean): ApiRelease {
        val managerRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
            prerelease,
        )

        return ApiRelease(
            managerRelease.tag,
            managerRelease.createdAt,
            managerRelease.releaseNote,
            managerRelease.assets.first(configurationRepository.manager.assetRegex).downloadUrl,
        )
    }

    suspend fun latestVersion(prerelease: Boolean): ApiReleaseVersion {
        val managerRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
            prerelease,
        )

        return ApiReleaseVersion(managerRelease.tag)
    }

    suspend fun latestDownloadersRelease(prerelease: Boolean): ApiRelease {
        val downloaderPluginsRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.downloadersRepository,
            prerelease,
        )

        return ApiRelease(
            downloaderPluginsRelease.tag,
            downloaderPluginsRelease.createdAt,
            downloaderPluginsRelease.releaseNote,
            downloaderPluginsRelease.assets.first(configurationRepository.manager.downloadersAssetRegex).downloadUrl,
        )
    }

    suspend fun latestDownloadersVersion(prerelease: Boolean): ApiReleaseVersion {
        val downloaderPluginsRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.downloadersRepository,
            prerelease,
        )

        return ApiReleaseVersion(downloaderPluginsRelease.tag)
    }
}
