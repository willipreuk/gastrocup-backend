package routes

import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.pendingUser() {
    post("/invite/{token}") {

    }
}