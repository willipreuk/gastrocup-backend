@file:Suppress("EXPERIMENTAL_API_USAGE")

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.main() {

    val db = DB()
    db.createSchemas()

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Routing)
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