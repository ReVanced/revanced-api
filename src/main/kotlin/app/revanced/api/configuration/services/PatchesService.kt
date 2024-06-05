package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.ConfigurationRepository
import app.revanced.api.configuration.repository.backend.BackendRepository
import app.revanced.api.configuration.schema.APIAsset
import app.revanced.api.configuration.schema.APIRelease
import app.revanced.api.configuration.schema.APIReleaseVersion
import app.revanced.library.PatchUtils
import app.revanced.patcher.PatchBundleLoader
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

internal class PatchesService(
    private val backendRepository: BackendRepository,
    private val configurationRepository: ConfigurationRepository,
) {
    private val patchesListCache = Caffeine
        .newBuilder()
        .maximumSize(1)
        .build<String, ByteArray>()

    suspend fun latestRelease(): APIRelease {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patchesRepository,
        )
        val integrationsReleases = withContext(Dispatchers.Default) {
            configurationRepository.integrationsRepositoryNames.map {
                async { backendRepository.release(configurationRepository.organization, it) }
            }
        }.awaitAll()

        val assets = (patchesRelease.assets + integrationsReleases.flatMap { it.assets })
            .map { APIAsset(it.downloadUrl) }
            .filter { it.type != APIAsset.Type.UNKNOWN }
            .toSet()

        return APIRelease(
            patchesRelease.tag,
            patchesRelease.createdAt,
            patchesRelease.releaseNote,
            assets,
        )
    }

    suspend fun latestVersion(): APIReleaseVersion {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patchesRepository,
        )

        return APIReleaseVersion(patchesRelease.tag)
    }

    suspend fun list(): ByteArray {
        val patchesRelease = backendRepository.release(
            configurationRepository.organization,
            configurationRepository.patchesRepository,
        )

        return patchesListCache.getIfPresent(patchesRelease.tag) ?: run {
            val downloadUrl = patchesRelease.assets
                .map { APIAsset(it.downloadUrl) }
                .find { it.type == APIAsset.Type.PATCHES }
                ?.downloadUrl

            val patches = kotlin.io.path.createTempFile().toFile().apply {
                outputStream().use { URL(downloadUrl).openStream().copyTo(it) }
            }.let { file ->
                PatchBundleLoader.Jar(file).also { file.delete() }
            }

            ByteArrayOutputStream().use { stream ->
                PatchUtils.Json.serialize(patches, outputStream = stream)

                stream.toByteArray()
            }.also {
                patchesListCache.put(patchesRelease.tag, it)
            }
        }
    }
}
