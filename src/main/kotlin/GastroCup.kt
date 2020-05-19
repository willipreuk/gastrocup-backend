import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.routing.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {}
    }
    install(Routing) {

    }
}