package routes

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import models.Role
import models.User
import models.Users
import org.jetbrains.exposed.sql.transactions.transaction

data class UserPostData(val email: String, val password: String, val surname: String, val name: String, val role: Role)
data class UserPutData(val email: String?, val password: String?, val surname: String?, val name: String?, val role: Role?)

fun Routing.user() {
    get("/users") {
        val users = transaction {
            User.all().map { user: User -> user.toModel() }
        }
        call.respond(mapOf("data" to users))
    }
    get("/user/{id}") {
        val user = Users.getById(call.parameters["id"])
        call.respond(mapOf("data" to user.toModel()))

    }
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
            newUser.hashPassword()
            newUser.toModel()
        }

        call.respond(mapOf("data" to user))

    }
    delete("/user/{id}") {
        val user = Users.getById(call.parameters["id"])
        transaction { user.delete() }
        call.respond(mapOf("data" to user.toModel().id))

    }
    put("/user/{id}") {
        val user = Users.getById(call.parameters["id"])
        val data = call.receive<UserPutData>()

        transaction {
            data.email?.let { user.email = it }
            data.name?.let { user.name = it }
            data.password?.let { user.password = it; user.hashPassword() }
            data.role?.let { user.role = it }
            data.surname?.let { user.surname = it }
        }

    }
}