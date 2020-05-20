import org.jetbrains.exposed.exceptions.ExposedSQLException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import routes.user
import java.lang.IllegalArgumentException
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
    install(Routing) {
        user()
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { _ ->
            call.response.status(HttpStatusCode.BadRequest)
        }
        exception<ExposedSQLException> { cause ->
            call.response.status(HttpStatusCode.BadRequest)

            // remove for production
            call.respond(mapOf("error" to cause.localizedMessage))
        }
    }
}