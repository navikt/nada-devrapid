package no.nav.nada.devrapid

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    runBlocking {
        Server.startServer(9090).start(false)
    }
}