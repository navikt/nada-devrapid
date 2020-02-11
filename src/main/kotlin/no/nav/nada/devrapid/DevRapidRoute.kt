package no.nav.nada.devrapid

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import no.nav.nada.devrapid.schema.DevEvent

internal fun Routing.devRapidRoute(messageSender: MessageSender) {
    post("/") {
        val devEvent = call.receive<DevEvent>()
        messageSender.sendDevEvent(devEvent)
        call.respond(HttpStatusCode.Accepted, "OK")
    }
}