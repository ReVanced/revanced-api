package app.revanced.api.configuration.repository

import app.revanced.api.configuration.services.ManagerService
import app.revanced.api.configuration.services.PatchesService
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File
import java.nio.file.Path

/**
 * The repository storing the configuration for the API.
 *
 * @property organization The API backends organization name where the repositories for the patches and integrations are.
 * @property patches The source of the patches.
 * @property integrations The source of the integrations.
 * @property manager The source of the manager.
 * @property contributorsRepositoryNames The names of the repositories to get contributors from.
 * @property apiVersion The version to use for the API.
 * @property corsAllowedHosts The hosts allowed to make requests to the API.
 * @property endpoint The endpoint of the API.
 * @property oldApiEndpoint The endpoint of the old API to proxy requests to.
 * @property staticFilesPath The path to the static files to be served under the root path.
 * @property versionedStaticFilesPath The path to the static files to be served under a versioned path.
 */
@Serializable
internal class ConfigurationRepository(
    val organization: String,
    val patches: SignedAssetConfiguration,
    val integrations: SignedAssetConfiguration,
    val manager: AssetConfiguration,
    @SerialName("contributors-repositories")
    val contributorsRepositoryNames: Set<String>,
    @SerialName("api-version")
    val apiVersion: Int = 1,
    @SerialName("cors-allowed-hosts")
    val corsAllowedHosts: Set<String>,
    val endpoint: String,
    @SerialName("old-api-endpoint")
    val oldApiEndpoint: String,
    @Serializable(with = PathSerializer::class)
    @SerialName("static-files-path")
    val staticFilesPath: Path,
    @Serializable(with = PathSerializer::class)
    @SerialName("versioned-static-files-path")
    val versionedStaticFilesPath: Path,
) {
    /**
     * Am asset configuration whose asset is signed.
     *
     * [PatchesService] for example uses [BackendRepository] to get assets from its releases.
     * A release contains multiple assets.
     *
     * This configuration is used in [ConfigurationRepository]
     * to determine which release assets from repositories to get and to verify them.
     *
     * @property repository The repository in which releases are made to get an asset.
     * @property assetRegex The regex matching the asset name.
     * @property signatureAssetRegex The regex matching the signature asset name to verify the asset.
     * @property publicKeyFile The public key file to verify the signature of the asset.
     * @property publicKeyId The ID of the public key to verify the signature of the asset.
     */
    @Serializable
    internal class SignedAssetConfiguration(
        val repository: String,
        @Serializable(with = RegexSerializer::class)
        @SerialName("asset-regex")
        val assetRegex: Regex,
        @Serializable(with = RegexSerializer::class)
        @SerialName("signature-asset-regex")
        val signatureAssetRegex: Regex,
        @Serializable(with = FileSerializer::class)
        @SerialName("public-key-file")
        val publicKeyFile: File,
        @SerialName("public-key-id")
        val publicKeyId: Long,
    )

    /**
     * Am asset configuration.
     *
     * [ManagerService] for example uses [BackendRepository] to get assets from its releases.
     * A release contains multiple assets.
     *
     * This configuration is used in [ConfigurationRepository]
     * to determine which release assets from repositories to get and to verify them.
     *
     * @property repository The repository in which releases are made to get an asset.
     * @property assetRegex The regex matching the asset name.
     */
    @Serializable
    internal class AssetConfiguration(
        val repository: String,
        @Serializable(with = RegexSerializer::class)
        @SerialName("asset-regex")
        val assetRegex: Regex,
    )
}

private object RegexSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Regex) = encoder.encodeString(value.pattern)

    override fun deserialize(decoder: Decoder) = Regex(decoder.decodeString())
}

private object FileSerializer : KSerializer<File> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("File", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: File) = encoder.encodeString(value.path)

    override fun deserialize(decoder: Decoder) = File(decoder.decodeString())
}

private object PathSerializer : KSerializer<Path> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder) = Path.of(decoder.decodeString())
}
