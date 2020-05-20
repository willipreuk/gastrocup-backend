package routes

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import models.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException

data class TeamPostData(val name: String, val leader: Int, val members: List<Int>)

fun Routing.team() {
    authenticate("Admin") {
        get("/teams") {
            val teams = transaction {
                Team.all().map { team -> team.toModel() }
            }
            call.respond(mapOf("data" to teams))
        }
        post("/team") {
            val data = call.receive<TeamPostData>()

            val leaderData = transaction {
                User.findById(data.leader)
            } ?: throw IllegalArgumentException()

            val membersData = transaction {
                User.find { Users.id inList data.members }
            }
            transaction {
                if (membersData.toList().isEmpty()) {
                    throw IllegalArgumentException()
                }
            }

            val team = transaction {
                Team.new {
                    name = data.name
                    leader = leaderData
                }
            }
            transaction {
                team.members = membersData
            }

            val res = transaction {
                team.toModel()
            }
            call.respond(mapOf("data" to res))
        }
    }
}