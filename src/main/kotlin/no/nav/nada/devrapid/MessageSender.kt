package no.nav.nada.devrapid

import no.nav.nada.devrapid.schema.DevEvent

interface MessageSender {
    suspend fun sendDevEvent(devEvent: DevEvent)
}