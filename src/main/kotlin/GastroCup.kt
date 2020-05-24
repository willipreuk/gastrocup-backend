@file:Suppress("EXPERIMENTAL_API_USAGE")

import com.google.gson.*
import helper.Mailer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import models.PendingUser
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.Type
import java.time.LocalDateTime


fun Application.main() {

    val db = DB()
    db.createSchemas()

    val mailer = Mailer()
    val pendingUser = transaction {
        PendingUser.findById(1)
    }
    pendingUser?.sendInvitationEmail(mailer)

    install(DefaultHeaders)
    install(CallLogging)
    install(Routing)
    install(Compression)
    install(AutoHeadResponse)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            registerTypeAdapter(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime?> {
                override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                    return JsonPrimitive(src.toString())
                }
            })
        }
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { cause ->
            call.response.status(HttpStatusCode.BadRequest)

            // remove for production
            call.respond(mapOf("error" to cause.localizedMessage))
        }
        exception<ExposedSQLException> { cause ->
            call.response.status(HttpStatusCode.BadRequest)

            // remove for production
            call.respond(mapOf("error" to cause.localizedMessage))
        }
        exception<NoSuchElementException> { cause ->
            call.response.status(HttpStatusCode.NotFound)

            // remove for production
            call.respond(mapOf("error" to cause.localizedMessage))
        }
    }
}