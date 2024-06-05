package app.revanced.api.configuration.services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.*

internal class OldApiService(private val client: HttpClient) {
    @OptIn(InternalAPI::class)
    suspend fun proxy(call: ApplicationCall) {
        val channel = call.request.receiveChannel()
        val size = channel.availableForRead
        val byteArray = ByteArray(size)
        channel.readFully(byteArray)

        val response: HttpResponse = client.request(call.request.uri) {
            method = call.request.httpMethod

            headers {
                appendAll(
                    call.request.headers.filter { key, _ ->
                        !(
                            key.equals(HttpHeaders.ContentType, ignoreCase = true) ||
                                key.equals(HttpHeaders.ContentLength, ignoreCase = true) ||
                                key.equals(HttpHeaders.Host, ignoreCase = true)
                            )
                    },
                )
            }

            when (call.request.httpMethod) {
                HttpMethod.Post,
                HttpMethod.Put,
                HttpMethod.Patch,
                HttpMethod.Delete,
                -> body = ByteArrayContent(byteArray, call.request.contentType())
            }
        }

        val headers = response.headers

        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val contentLength: Long? = headers[HttpHeaders.ContentLength]?.toLong()
            override val contentType = headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
            override val headers: Headers = Headers.build {
                appendAll(
                    headers.filter { key, _ ->
                        !key.equals(
                            HttpHeaders.ContentType,
                            ignoreCase = true,
                        ) &&
                            !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
                    },
                )
            }
            override val status = response.status

            override suspend fun writeTo(channel: ByteWriteChannel) {
                response.content.copyAndClose(channel)
            }
        })
    }
}
