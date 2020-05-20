@file:Suppress("EXPERIMENTAL_API_USAGE")

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.jetbrains.exposed.exceptions.ExposedSQLException
import routes.user
import java.text.DateFormat

fun Application.main() {

    val db = DB()
    db.createSchemas()

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
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