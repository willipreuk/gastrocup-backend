import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import models.User
import java.util.*

val users = Collections.synchronizedList(mutableListOf(
        User("willi", "willi"),
        User("johanna", "johanna")
))

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {}
    }
    install(Routing) {
        get("/") {
            call.respond(mapOf("users" to synchronized(users) {
                users.toList()
            }))
        }
        post("/user") {
            val user = call.receive<User>()
            users += user
            call.respond(mapOf("OK" to true))
        }
    }
}