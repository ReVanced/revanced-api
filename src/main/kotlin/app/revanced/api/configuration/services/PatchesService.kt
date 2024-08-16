package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.*
import app.revanced.library.serializeTo
import app.revanced.patcher.patch.loadPatchesFromJar
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
    suspend fun latestRelease(): APIRelease<APIPatchesAsset> {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
        )

        val integrationsRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.integrations.repository,
        )

        fun ConfigurationRepository.SignedAssetConfiguration.asset(
            release: BackendRepository.BackendOrganization.BackendRepository.BackendRelease,
            assetName: APIAssetName,
        ) = APIPatchesAsset(
            release.assets.first(assetRegex).downloadUrl,
            release.assets.first(signatureAssetRegex).downloadUrl,
            assetName,
        )

        val patchesAsset = configurationRepository.patches.asset(
            patchesRelease,
            APIAssetName.PATCHES,
        )
        val integrationsAsset = configurationRepository.integrations.asset(
            integrationsRelease,
            APIAssetName.INTEGRATION,
        )

        return APIRelease(
            patchesRelease.tag,
            patchesRelease.createdAt,
            patchesRelease.releaseNote,
            listOf(patchesAsset, integrationsAsset),
        )
    }

    suspend fun latestVersion(): APIReleaseVersion {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
        )

        return APIReleaseVersion(patchesRelease.tag)
    }

    private val patchesListCache = Caffeine
        .newBuilder()
        .maximumSize(1)
        .build<String, ByteArray>()

    suspend fun list(): ByteArray {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
        )

        return withContext(Dispatchers.IO) {
            patchesListCache.get(patchesRelease.tag) {
                val patchesDownloadUrl = patchesRelease.assets
                    .first(configurationRepository.patches.assetRegex).downloadUrl

                val signatureDownloadUrl = patchesRelease.assets
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
                    loadPatchesFromJar(setOf(patchesFile))
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

    fun publicKeys(): APIAssetPublicKeys {
        fun readPublicKey(
            getSignedAssetConfiguration: ConfigurationRepository.() -> ConfigurationRepository.SignedAssetConfiguration,
        ) = configurationRepository.getSignedAssetConfiguration().publicKeyFile.readText()

        return APIAssetPublicKeys(
            readPublicKey { patches },
            readPublicKey { integrations },
        )
    }
}
