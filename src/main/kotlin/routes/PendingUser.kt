package routes

import helper.respondData
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.Routing
import io.ktor.routing.post
import models.*
import org.jetbrains.exposed.sql.transactions.transaction

data class InvitePostData(val name: String, val surname: String, val password: String)

fun Routing.pendingUser() {
    post("/invite/{token}") {
        val token = call.parameters["token"]
        if (token == null) {
            call.response.status(HttpStatusCode.Unauthorized)
            return@post
        }

        try {
            val pendingUser = transaction {
                PendingUser.find {
                    PendingUsers.invitationToken eq token
                }.first()
            }

            if (!pendingUser.invitationAccepted && pendingUser.invitationToken == token) {
                val data = call.receive<InvitePostData>()

                val newUser = transaction {
                    User.new {
                        name = data.name
                        surname = data.surname
                        email = pendingUser.email
                        password = Users.hashPassword(data.password)
                        role = Role.TeamMember
                    }
                }
                transaction {
                    pendingUser.team.members.plus(newUser)

                    pendingUser.invitationAccepted = true
                }

                val res = transaction { newUser.toModel() }

                call.respondData(res)
            } else {
                call.response.status(HttpStatusCode.Unauthorized)
            }
        } catch (e: NoSuchElementException) {
            call.response.status(HttpStatusCode.Unauthorized)
            return@post
        }
    }
}