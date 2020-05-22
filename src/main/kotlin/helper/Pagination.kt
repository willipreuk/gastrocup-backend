package helper

import io.ktor.application.ApplicationCall

data class Pagination(val offset: Long, val perPage: Int)

object PaginationHelper {
    fun paginate(call: ApplicationCall): Pagination {
        val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
        val perPage = call.request.queryParameters["perPage"]?.toInt() ?: 10

        return Pagination(offset, perPage)
    }
}