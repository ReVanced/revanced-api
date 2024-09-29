package app.revanced.api.configuration.services

import app.revanced.api.configuration.schema.APIToken
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.text.HexFormat

internal class AuthenticationService private constructor(
    private val issuer: String,
    private val validityInMin: Long,
    private val jwtSecret: String,
    private val authSHA256Digest: ByteArray,
) {
    @OptIn(ExperimentalStdlibApi::class)
    constructor(issuer: String, validityInMin: Long, jwtSecret: String, authSHA256DigestString: String) : this(
        issuer,
        validityInMin,
        jwtSecret,
        authSHA256DigestString.hexToByteArray(HexFormat.Default),
    )

    fun AuthenticationConfig.jwt() {
        jwt("jwt") {
            realm = "ReVanced"
            verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).withIssuer(issuer).build())
            // This is required and not optional. Authentication will fail if this is not present.
            validate { JWTPrincipal(it.payload) }
        }
    }

    fun AuthenticationConfig.digest() {
        digest("auth-digest") {
            realm = "ReVanced"
            algorithmName = "SHA-256"

            digestProvider { _, _ ->
                authSHA256Digest
            }
        }
    }

    fun newToken() = APIToken(
        JWT.create()
            .withIssuer(issuer)
            .withExpiresAt(Instant.now().plus(validityInMin, ChronoUnit.MINUTES))
            .sign(Algorithm.HMAC256(jwtSecret)),
    )
}
