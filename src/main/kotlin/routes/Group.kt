package routes

import helper.PaginationHelper
import helper.respondData
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import models.Group
import models.Groups
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction

data class GroupPostData(val name: String, val matchField: String)
data class GroupPutData(val name: String?, val matchField: String?)

fun Routing.group() {
    authenticate("Admin") {
        get("/groups") {
            val pagination = PaginationHelper.paginate(call)

            val groups = transaction {
                Group.all().limit(pagination.perPage, pagination.offset).map { it.toModel() }
            }
            val total = transaction {
                Group.all().count()
            }

            call.respondData(groups, total)
        }
        get("/group/{id}") {
            val group = Groups.getById(call.parameters["id"])

            val res = transaction { group.toModel() }
            call.respond(mapOf("data" to res))
        }
        post("/group") {
            val data = call.receive<GroupPostData>()

            val group = transaction {
                Group.new {
                    name = data.name
                    matchField = data.matchField
                }
            }

            val res = transaction { group.toModel() }
            call.respond(res)
        }
        put("/group/{id}") {
            val group = Groups.getById(call.parameters["id"])
            val data = call.receive<GroupPutData>()

            transaction {
                data.name?.let { group.name = it }
                data.matchField?.let { group.matchField = it }
            }

            val res = transaction { group.toModel() }
            call.respond(mapOf("data" to res))
        }
        delete("/group/{id}") {
            val group = Groups.getById(call.parameters["id"])
            try {
                transaction { group.delete() }
            } catch (e: Exception) {
                if (e is ExposedSQLException) {
                    call.response.status(HttpStatusCode.Conflict)
                    call.respond(mapOf("error" to "Please first remove all Teams from Group."))
                    return@delete
                }
            }

            call.respond(mapOf("data" to group.id.value))
        }
    }
}