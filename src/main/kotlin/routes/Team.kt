package routes

import helper.PaginationHelper
import helper.respondData
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.routing.*
import models.*
import org.jetbrains.exposed.sql.transactions.transaction

data class TeamPostData(val name: String, val leader: Int, val members: List<Int>, val group: Int)
data class TeamPutData(val name: String?, val leader: Int?, val members: List<Int>?, val group: Int?)

fun Routing.team() {
    authenticate("Admin") {
        get("/teams") {
            val pagination = PaginationHelper.paginate(call)

            val teams = transaction {
                Team.all().limit(pagination.perPage, pagination.offset).map { team -> team.toModel() }
            }
            val total = transaction { Team.all().count() }
            call.respondData(teams, total)
        }
        get("/team/{id}") {
            val team = Teams.getById(call.parameters["id"])

            val res = transaction { team.toModel() }
            call.respondData(res)
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
                    group = Group.findById(data.group) ?: throw IllegalArgumentException()
                }
            }
            transaction {
                team.members = membersData
            }

            val res = transaction {
                team.toModel()
            }
            call.respondData(res)
        }
        delete("/team/{id}") {
            val team = Teams.getById(call.parameters["id"])

            transaction { team.delete() }
            call.respondData(team.id.value)
        }
        put("/team/{id}") {
            val team = Teams.getById(call.parameters["id"])
            val data = call.receive<TeamPutData>()

            transaction {
                data.name?.let { team.name = it }
                data.leader?.let {
                    team.leader = Users.getById(it.toString())
                }
                data.members?.let {
                    team.members = User.find { Users.id inList it }
                }
                data.group?.let {
                    team.group = Group.findById(it) ?: throw IllegalArgumentException()
                }
            }

            val res = transaction { team.toModel() }
            call.respondData(res)
        }
    }
}