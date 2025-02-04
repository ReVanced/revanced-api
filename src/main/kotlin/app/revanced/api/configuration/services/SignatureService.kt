package app.revanced.api.configuration.services

import com.github.benmanes.caffeine.cache.Caffeine
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
import java.io.File
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest

internal class SignatureService {
    private val signatureCache = Caffeine
        .newBuilder()
        .maximumSize(2) // 2 because currently only the latest release and prerelease patches are needed.
        .build<ByteArray, Boolean>() // Hash -> Verified.

    fun verify(
        file: File,
        signatureDownloadUrl: String,
        publicKeyFile: File,
        publicKeyId: Long,
    ): Boolean {
        val fileBytes = file.readBytes()

        return signatureCache.get(MessageDigest.getInstance("SHA-256").digest(fileBytes)) {
            verify(
                fileBytes = fileBytes,
                signatureInputStream = URL(signatureDownloadUrl).openStream(),
                publicKeyFileInputStream = publicKeyFile.inputStream(),
                publicKeyId = publicKeyId,
            )
        }
    }

    private fun verify(
        fileBytes: ByteArray,
        signatureInputStream: InputStream,
        publicKeyFileInputStream: InputStream,
        publicKeyId: Long,
    ) = getSignature(signatureInputStream).apply {
        init(BcPGPContentVerifierBuilderProvider(), getPublicKey(publicKeyFileInputStream, publicKeyId))
        update(fileBytes)
    }.verify()

    private fun getPublicKey(
        publicKeyFileInputStream: InputStream,
        publicKeyId: Long,
    ): PGPPublicKey {
        val decoderStream = PGPUtil.getDecoderStream(publicKeyFileInputStream)
        val pgpPublicKeyRingCollection = PGPPublicKeyRingCollection(decoderStream, BcKeyFingerprintCalculator())
        val publicKeyRing = pgpPublicKeyRingCollection.getPublicKeyRing(publicKeyId)
            ?: throw IllegalArgumentException("Can't find public key ring with ID $publicKeyId.")

        return publicKeyRing.getPublicKey(publicKeyId)
            ?: throw IllegalArgumentException("Can't find public key with ID $publicKeyId.")
    }

    private fun getSignature(inputStream: InputStream): PGPSignature {
        val decoderStream = PGPUtil.getDecoderStream(inputStream)
        val pgpSignatureList = PGPObjectFactory(decoderStream, BcKeyFingerprintCalculator()).first {
            it is PGPSignatureList
        } as PGPSignatureList

        return pgpSignatureList.first()
    }
}
