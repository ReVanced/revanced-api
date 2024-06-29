package app.revanced.api.configuration.services

import com.github.benmanes.caffeine.cache.Caffeine
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider
import java.io.File
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.security.Security

internal class SignatureService {
    private val signatureCache = Caffeine
        .newBuilder()
        .maximumSize(2) // Assuming this is enough for patches and integrations.
        .build<ByteArray, Boolean>() // Hash -> Verified.

    fun verify(
        file: File,
        signatureDownloadUrl: String,
        publicKeyFile: File,
    ): Boolean {
        val fileBytes = file.readBytes()

        return signatureCache.get(MessageDigest.getInstance("SHA-256").digest(fileBytes)) {
            verify(
                fileBytes = fileBytes,
                signatureInputStream = URL(signatureDownloadUrl).openStream(),
                publicKeyInputStream = publicKeyFile.inputStream(),
            )
        }
    }

    private fun verify(
        fileBytes: ByteArray,
        signatureInputStream: InputStream,
        publicKeyInputStream: InputStream,
    ) = getSignature(signatureInputStream).apply {
        init(BcPGPContentVerifierBuilderProvider(), getPublicKey(publicKeyInputStream))
        update(fileBytes)
    }.verify()

    private fun getPublicKey(publicKeyInputStream: InputStream): PGPPublicKey {
        val decoderStream = PGPUtil.getDecoderStream(publicKeyInputStream)

        PGPPublicKeyRingCollection(decoderStream, BcKeyFingerprintCalculator()).forEach { keyRing ->
            keyRing.publicKeys.forEach { publicKey ->
                if (publicKey.isEncryptionKey) {
                    return publicKey
                }
            }
        }

        throw IllegalArgumentException("Can't find encryption key in key ring.")
    }

    private fun getSignature(inputStream: InputStream): PGPSignature {
        val decoderStream = PGPUtil.getDecoderStream(inputStream)
        val pgpObjectFactory = PGPObjectFactory(decoderStream, BcKeyFingerprintCalculator())
        val signatureList = pgpObjectFactory.nextObject() as PGPSignatureList

        return signatureList.first()
    }

    private companion object {
        init {
            Security.addProvider(BouncyCastleProvider())
        }
    }
}
