@file:Suppress("EXPERIMENTAL_API_USAGE")

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import models.Role
import models.User
import models.Users
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class Token(val token: String)
data class LoginPostData(val email: String, val password: String)


fun Application.auth() {
    val domain = environment.config.property("jwt.domain").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val validityInMs = environment.config.property("jwt.validityInMs").getString().toInt()

    val algorithm = Algorithm.HMAC512(secret)

    val verifier = JWT.require(algorithm).withIssuer(domain).withAudience(audience).build()

    fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

    fun makeToken(user: User): String = JWT.create()
            .withSubject("Authentication")
            .withIssuer(domain)
            .withClaim("id", user.id.value)
            .withClaim("role", user.readValues[Users.role].name)
            .withExpiresAt(getExpiration())
            .withAudience(audience)
            .sign(algorithm)

    fun validate(payload: Payload, requiredRole: Role): Boolean {
        with(payload) {
            val id = getClaim("id")
            val role = getClaim("role")
            return (id.isNull || Role.valueOf(role.asString()) != requiredRole)
        }
    }

    install(Authentication) {
        jwt(name = "LoggedIn") {
            verifier(verifier)
            realm = domain
            validate {
                with(it.payload) {
                    val id = getClaim("id")
                    if(id.isNull) {
                        null
                    } else {
                        JWTPrincipal(it.payload)
                    }
                }
            }
        }
        jwt(name = "Admin") {
            verifier(verifier)
            realm = domain
            validate {
                if (!validate(it.payload, Role.Admin))
                    JWTPrincipal(it.payload)
                else
                    null
            }
        }
        jwt(name = "Referee") {
            verifier(verifier)
            realm = domain
            validate {
                if (!validate(it.payload, Role.Referee))
                    JWTPrincipal(it.payload)
                else {
                    null
                }
            }
        }
    }

    routing {
        post("/login") {
            val data = call.receive<LoginPostData>()

            val user = transaction {
                User.find { Users.email eq data.email }.first()
            }

            if (user.password != Users.hashPassword(data.password)) {
                call.response.status(HttpStatusCode.Unauthorized)
                return@post
            }
            call.respond(mapOf("data" to Token(makeToken(user))))
        }
    }
}