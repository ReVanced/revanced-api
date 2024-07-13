package app.revanced.api.configuration.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*
import kotlin.text.HexFormat
import kotlin.time.Duration.Companion.minutes

internal class AuthService private constructor(
    private val issuer: String,
    private val validityInMin: Int,
    private val jwtSecret: String,
    private val authSHA256Digest: ByteArray,
) {
    @OptIn(ExperimentalStdlibApi::class)
    constructor(issuer: String, validityInMin: Int, jwtSecret: String, authSHA256DigestString: String) : this(
        issuer,
        validityInMin,
        jwtSecret,
        authSHA256DigestString.hexToByteArray(HexFormat.Default),
    )

    val configureSecurity: Application.() -> Unit = {
        install(Authentication) {
            jwt("jwt") {
                realm = "ReVanced"

                verifier(JWT.require(Algorithm.HMAC256(jwtSecret)).withIssuer(issuer).build())
            }

            digest("auth-digest") {
                realm = "ReVanced"
                algorithmName = "SHA-256"

                digestProvider { _, _ ->
                    authSHA256Digest
                }
            }
        }
    }

    fun newToken(): String = JWT.create()
        .withIssuer(issuer)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMin.minutes.inWholeMilliseconds))
        .sign(Algorithm.HMAC256(jwtSecret))
}
