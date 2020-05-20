@file:Suppress("EXPERIMENTAL_API_USAGE")

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import models.User
import models.Users
import java.util.*

data class Token(val token: String)

fun Application.auth() {
    val issuer = environment.config.property("jwt/issuer").getString()
    val domain = environment.config.property("jwt/domain").getString()
    val secret = environment.config.property("jwt/secret").getString()
    val audience = environment.config.property("jwt/audience").getString()
    val validityInMs = environment.config.property("jwt/validityInMs").getString().toInt()
    val algorithm = Algorithm.HMAC512(secret)

    val verifier = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()

    fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

    fun makeToken(user: User): String = JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withClaim("id", user.id.value)
            .withClaim("role", user.readValues[Users.role].name)
            .withExpiresAt(getExpiration())
            .sign(algorithm)

    authentication {
        jwt {
            verifier(verifier)
            realm = domain
            validate {
                with(it.payload) {
                    val id = getClaim("id").isNull
                    if (id)
                        null
                    else
                        JWTPrincipal(it.payload)
                }
            }
        }
    }
}