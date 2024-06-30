package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.BackendRepository
import app.revanced.api.configuration.repository.BackendRepository.BackendOrganization.BackendRepository.BackendRelease.Companion.first
import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.schema.*
import app.revanced.library.PatchUtils
import app.revanced.patcher.PatchBundleLoader
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

internal class PatchesService(
    private val signatureService: SignatureService,
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    suspend fun latestRelease(): APIRelease {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patches.repository,
        )

        val integrationsRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.integrations.repository,
        )

        fun ConfigurationRepository.AssetConfiguration.asset(
            release: BackendRepository.BackendOrganization.BackendRepository.BackendRelease,
            assetType: APIAssetType,
        ) = APIAsset(
            release.assets.first(assetRegex).downloadUrl,
            release.assets.first(signatureAssetRegex).downloadUrl,
            assetType,
        )

        val patchesAsset = configurationRepository.patches.asset(
            patchesRelease,
            APIAssetType.PATCHES,
        )
        val integrationsAsset = configurationRepository.integrations.asset(
            integrationsRelease,
            APIAssetType.INTEGRATION,
        )

        return APIRelease(
            patchesRelease.tag,
            patchesRelease.createdAt,
            patchesRelease.releaseNote,
            setOf(patchesAsset, integrationsAsset),
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
                    )
                ) {
                    PatchBundleLoader.Jar(patchesFile)
                } else {
                    // Use an empty set of patches if the signature is invalid.
                    emptySet()
                }

                patchesFile.delete()

                ByteArrayOutputStream().use { stream ->
                    PatchUtils.Json.serialize(patches, outputStream = stream)

                    stream.toByteArray()
                }
            }
        }
    }

    fun publicKeys(): APIAssetPublicKeys {
        fun publicKeyBase64(getAssetConfiguration: ConfigurationRepository.() -> ConfigurationRepository.AssetConfiguration) =
            configurationRepository.getAssetConfiguration().publicKeyFile.readBytes().encodeBase64()

        return APIAssetPublicKeys(
            publicKeyBase64 { patches },
            publicKeyBase64 { integrations },
        )
    }
}
