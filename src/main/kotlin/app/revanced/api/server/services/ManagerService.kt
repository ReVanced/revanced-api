package app.revanced.api.server.services

import app.revanced.api.server.ApiRelease
import app.revanced.api.server.ApiReleaseSimple
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
    ): List<ApiReleaseSimple> {
        val releases = backendRepository.releases(
            configurationRepository.organization,
            configurationRepository.manager.repository,
            100,
        ).let {
            if (!prerelease) it.filter { release -> !release.prerelease } else it
        }

        return releases.map {
            ApiReleaseSimple(
                it.tag,
                it.createdAt,
                it.releaseNote,
            )
        }
    }

    suspend fun latestRelease(prerelease: Boolean): ApiRelease {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
            prerelease,
        )

        return ApiRelease(
            release.tag,
            release.createdAt,
            release.releaseNote,
            release.assets.first(configurationRepository.manager.assetRegex).downloadUrl,
        )
    }

    suspend fun latestVersion(prerelease: Boolean): ApiReleaseVersion {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.repository,
            prerelease,
        )

        return ApiReleaseVersion(release.tag)
    }

    suspend fun latestDownloadersRelease(prerelease: Boolean): ApiRelease {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.downloadersRepository,
            prerelease,
        )

        return ApiRelease(
            release.tag,
            release.createdAt,
            release.releaseNote,
            release.assets.first(configurationRepository.manager.downloadersAssetRegex).downloadUrl,
        )
    }

    suspend fun latestDownloadersVersion(prerelease: Boolean): ApiReleaseVersion {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.manager.downloadersRepository,
            prerelease,
        )

        return ApiReleaseVersion(release.tag)
    }
}
