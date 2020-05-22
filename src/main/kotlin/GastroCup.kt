@file:Suppress("EXPERIMENTAL_API_USAGE")

import com.google.gson.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.lang.reflect.Type
import java.time.LocalDateTime


fun Application.main() {

    val db = DB()
    db.createSchemas()

    install(DefaultHeaders)
    install(CallLogging)
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