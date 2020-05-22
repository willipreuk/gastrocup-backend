package helper

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

data class TotalDataResponse(val data: Any, val total: Long)
data class DataResponse(val data: Any)

suspend fun ApplicationCall.respondData(data: Any, total: Long = -1) {
    if (total == (-1).toLong()) {
        this.respond(DataResponse(data))
    } else {
        this.respond(TotalDataResponse(data, total))
    }
    this.response.status(HttpStatusCode.OK)
}
