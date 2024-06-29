package app.revanced.api.configuration.repository

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

/**
 * The repository storing the configuration for the API.
 *
 * @property organization The API backends organization name where the repositories for the patches and integrations are.
 * @property patches The source of the patches.
 * @property integrations The source of the integrations.
 * @property contributorsRepositoryNames The names of the repositories to get contributors from.
 * @property apiVersion The version to use for the API.
 * @property host The host of the API to configure CORS.
 */
@Serializable
internal class ConfigurationRepository(
    val organization: String,
    val patches: AssetConfiguration,
    val integrations: AssetConfiguration,
    @SerialName("contributors-repositories")
    val contributorsRepositoryNames: Set<String>,
    @SerialName("api-version")
    val apiVersion: Int = 1,
    val host: String,
) {
    /**
     * An asset configuration.
     *
     * [PatchesService] uses [BackendRepository] to get assets from its releases.
     * A release contains multiple assets.
     *
     * This configuration is used in [ConfigurationRepository]
     * to determine which release assets from repositories to get and to verify them.
     *
     * @property repository The repository in which releases are made to get an asset.
     * @property assetRegex The regex matching the asset name.
     * @property signatureAssetRegex The regex matching the signature asset name to verify the asset.
     * @property publicKeyFile The public key file to verify the signature of the asset.
     */
    @Serializable
    internal class AssetConfiguration(
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
