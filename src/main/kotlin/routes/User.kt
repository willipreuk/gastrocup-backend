package routes

import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import models.Role
import models.User
import models.Users
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

data class UserPostData(val email: String, val password: String, val surname: String, val name: String, val role: Role)
data class UserPutData(val email: String?, val password: String?, val surname: String?, val name: String?, val role: Role?)

fun Routing.user() {
    authenticate("Admin") {
        post("/user") {
            val postData = call.receive<UserPostData>()

            val user = transaction {
                val newUser = User.new {
                    email = postData.email
                    password = postData.password
                    surname = postData.surname
                    name = postData.name
                    role = postData.role
                }
                newUser.password = Users.hashPassword(postData.password)
                newUser.toModel()
            }

            call.respond(mapOf("data" to user))

        }
        get("/users") {
            val users = transaction {
                User.all().map { user: User -> user.toModel() }
            }
            call.respond(mapOf("data" to users))
        }
        delete("/user/{id}") {
            val user = Users.getById(call.parameters["id"])
            transaction { user.delete() }
            call.respond(mapOf("data" to user.toModel().id))

        }
    }
    authenticate("LoggedIn") {

        fun checkSameUser(principal: JWTPrincipal?, id: String) {
            val principalPayload = principal?.payload ?: throw IllegalArgumentException()
            if (Role.valueOf(principalPayload.getClaim("role").asString()) != Role.Admin) {
                if (principalPayload.id != id) {
                    throw IllegalArgumentException()
                }
            }
        }

        put("/user/{id}") {
            checkSameUser(call.authentication.principal<JWTPrincipal>(), call.parameters["id"]!!)

            val user = Users.getById(call.parameters["id"])
            val data = call.receive<UserPutData>()

            transaction {
                data.email?.let { user.email = it }
                data.name?.let { user.name = it }
                data.password?.let { user.password = Users.hashPassword(it) }
                data.role?.let { user.role = it }
                data.surname?.let { user.surname = it }
            }

            call.respond(mapOf("data" to user))
        }
        get("/user/{id}") {
            checkSameUser(call.authentication.principal<JWTPrincipal>(), call.parameters["id"]!!)

            val user = Users.getById(call.parameters["id"])
            call.respond(mapOf("data" to user.toModel()))
        }
    }
}