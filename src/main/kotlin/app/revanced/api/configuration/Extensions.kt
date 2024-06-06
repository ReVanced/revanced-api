package app.revanced.api.configuration

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondOrNotFound(value: Any?) = respond(value ?: HttpStatusCode.NotFound)
