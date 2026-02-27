package app.revanced.api.server.services

import app.revanced.api.server.ApiAssetPublicKey
import app.revanced.api.server.ApiRelease
import app.revanced.api.server.ApiReleaseSimple
import app.revanced.api.server.ApiReleaseVersion
import app.revanced.api.server.repositories.BackendRepository
import app.revanced.api.server.repositories.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.server.repositories.ConfigurationRepository
import app.revanced.library.serializeTo
import app.revanced.patcher.patch.loadPatches
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

internal class PatchesService(
    private val signatureService: SignatureService,
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun history(
        prerelease: Boolean
    ): List<ApiReleaseSimple> {
        val releases = backendRepository.releases(
            configurationRepository.organization,
            configurationRepository.patches.repository,
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
            configurationRepository.patches.repository,
            prerelease,
        )

        return ApiRelease(
            release.tag,
            release.createdAt,
            release.releaseNote,
            release.assets.first(configurationRepository.patches.assetRegex).downloadUrl,
            release.assets.first(configurationRepository.patches.signatureAssetRegex).downloadUrl,
        )
    }

    suspend fun latestVersion(prerelease: Boolean): ApiReleaseVersion {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
            prerelease,
        )

        return ApiReleaseVersion(release.tag)
    }

    private val patchesListCache = Caffeine
        .newBuilder()
        .maximumSize(1)
        .build<String, ByteArray>()

    suspend fun list(prerelease: Boolean): ByteArray {
        val release = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
            prerelease,
        )

        return withContext(Dispatchers.IO) {
            patchesListCache.get(release.tag) {
                val patchesDownloadUrl = release.assets
                    .first(configurationRepository.patches.assetRegex).downloadUrl

                val signatureDownloadUrl = release.assets
                    .first(configurationRepository.patches.signatureAssetRegex).downloadUrl

                val patchesFile = kotlin.io.path.createTempFile().toFile().apply {
                    outputStream().use { URL(patchesDownloadUrl).openStream().copyTo(it) }
                }

                val patches = if (
                    signatureService.verify(
                        patchesFile,
                        signatureDownloadUrl,
                        configurationRepository.patches.publicKeyFile,
                        configurationRepository.patches.publicKeyId,
                    )
                ) {
                    loadPatches(patchesFile)
                } else {
                    // Use an empty set of patches if the signature is invalid.
                    emptySet()
                }

                patchesFile.delete()

                ByteArrayOutputStream().use { stream ->
                    patches.serializeTo(outputStream = stream)

                    stream.toByteArray()
                }
            }
        }
    }

    fun publicKey() = ApiAssetPublicKey(configurationRepository.patches.publicKeyFile.readText())
}
