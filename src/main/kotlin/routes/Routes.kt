package routes

import io.ktor.application.Application
import io.ktor.routing.routing

fun Application.routes() {
    routing {
        user()
        team()
        group()
    }
}