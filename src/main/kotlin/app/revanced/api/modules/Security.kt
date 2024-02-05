package app.revanced.api.modules

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.get
import java.util.*
import kotlin.time.Duration.Companion.minutes

class AuthService(
    private val issuer: String,
    private val validityInMin: Int,
    private val jwtSecret: String,
    private val basicUsername: String,
    private val basicPassword: String,
) {
    val configureSecurity: Application.() -> Unit = {
        install(Authentication) {
            jwt("jwt") {
                verifier(
                    JWT.require(Algorithm.HMAC256(jwtSecret))
                        .withIssuer(issuer)
                        .build(),
                )
                validate { credential -> JWTPrincipal(credential.payload) }
            }

            basic("basic") {
                validate { credentials ->
                    if (credentials.name == basicUsername && credentials.password == basicPassword) {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun newToken(): String {
        return JWT.create()
            .withIssuer(issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMin.minutes.inWholeMilliseconds))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}

fun Application.configureSecurity() {
    val configureSecurity = get<AuthService>().configureSecurity
    configureSecurity()
}
