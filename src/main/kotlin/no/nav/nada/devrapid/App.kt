package no.nav.nada.devrapid

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    runBlocking {
        val konfig = Configuration()
        Server.startServer(konfig.application.port).start(false)
    }
}
