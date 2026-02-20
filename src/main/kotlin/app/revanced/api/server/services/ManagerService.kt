package app.revanced.api.server.services

import app.revanced.api.server.ApiRelease
import app.revanced.api.server.ApiReleaseHistory
import app.revanced.api.server.ApiReleaseVersion
import app.revanced.api.server.repositories.BackendRepository
import app.revanced.api.server.repositories.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.server.repositories.ConfigurationRepository

internal class ManagerService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun history(
        prerelease: Boolean
    ): ApiReleaseHistory? {
        val historyFile = configurationRepository.manager.historyFile ?: return null

        val path = (
                if (prerelease) configurationRepository.backendServicePrereleaseBranch
                else configurationRepository.backendServiceMainBranch
                ) + "/" + historyFile

        return ApiReleaseHistory(
            backendRepository.file(
                configurationRepository.organization,
                configurationRepository.manager.repository,
                path,
            )
        )
    }

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
