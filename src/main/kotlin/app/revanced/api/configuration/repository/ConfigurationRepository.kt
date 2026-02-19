package app.revanced.api.configuration.repository

import app.revanced.api.configuration.APIAbout
import app.revanced.api.configuration.services.ManagerService
import app.revanced.api.configuration.services.PatchesService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * The repository storing the configuration for the API.
 *
 * @property organization The API backends organization name where the repositories are.
 * @property patches The configuration for patches.
 * @property manager The configuration for the manager.
 * @property contributorsRepositoryNames The friendly name of repos mapped to the repository names to get contributors from.
 * @property backendServiceName The name of the backend service to use for the repositories, contributors, etc.
 * @property apiVersion The version to use for the API.
 * @property corsAllowedHosts The hosts allowed to make requests to the API.
 * @property endpoint The endpoint of the API.
 * @property staticFilesPath The path to the static files to be served under the root path.
 * @property versionedStaticFilesPath The path to the static files to be served under a versioned path.
 * @property about The path to the json file deserialized to [APIAbout]
 * (because com.akuleshov7.ktoml.Toml does not support nested tables).
 */
@Serializable
internal class ConfigurationRepository(
    val organization: String,
    val patches: PatchesConfiguration,
    val manager: ManagerConfiguration,
    @SerialName("contributors-repositories")
    val contributorsRepositoryNames: Map<String, String>,
    @SerialName("backend-service-name")
    val backendServiceName: String,
    @SerialName("api-version")
    val apiVersion: String = "v1",
    @SerialName("cors-allowed-hosts")
    val corsAllowedHosts: Set<String>,
    val endpoint: String,
    @Serializable(with = PathSerializer::class)
    @SerialName("static-files-path")
    val staticFilesPath: Path,
    @Serializable(with = PathSerializer::class)
    @SerialName("versioned-static-files-path")
    val versionedStaticFilesPath: Path,
    @Serializable(with = AboutSerializer::class)
    @SerialName("about-json-file-path")
    val about: APIAbout,
) {
    init {
        staticFilesPath.createDirectories()
        versionedStaticFilesPath.createDirectories()
    }

    /**
     * A configuration for [PatchesService].
     *
     * @property repository The patches repository.
     * @property assetRegex The regex matching the patches asset name
     * in releases from the patches repository. 
     * @property signatureAssetRegex The regex matching the patches signature asset name
     * in releases from the patches repository.
     * @property publicKeyFile The public key file to verify the signature of the patches asset
     * in releases from the patches repository.
     * @property publicKeyId The ID of the public key to verify the signature of the patches asset
     * in releases from the patches repository.
     */
    @Serializable
    internal class PatchesConfiguration(
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
     * A configuration for [ManagerService].

     * @property repository The manager repository.
     * @property assetRegex The regex matching the manager asset name
     * in releases from the manager repository.
     * @property downloadersRepository The manager downloaders repository.
     * @property downloadersAssetRegex The regex matching the manager downloaders asset name
     * in releases from the manager downloaders repository.
     */
    @Serializable
    internal class ManagerConfiguration(
        val repository: String,
        @Serializable(with = RegexSerializer::class)
        @SerialName("asset-regex")
        val assetRegex: Regex,
        @SerialName("downloaders-repository")
        val downloadersRepository: String,
        @Serializable(with = RegexSerializer::class)
        @SerialName("downloaders-asset-regex")
        val downloadersAssetRegex: Regex,
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

    override fun deserialize(decoder: Decoder): Path = Path.of(decoder.decodeString())
}

private object AboutSerializer : KSerializer<APIAbout> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("APIAbout", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: APIAbout) = error("Serializing APIAbout is not supported")

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json { namingStrategy = JsonNamingStrategy.SnakeCase }

    override fun deserialize(decoder: Decoder): APIAbout =
        json.decodeFromStream(File(decoder.decodeString()).inputStream())
}
