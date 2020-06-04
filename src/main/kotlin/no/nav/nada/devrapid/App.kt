package no.nav.nada.devrapid

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    runBlocking {
        val konfig = Configuration()
        logger.info { "Sender ready" }
        val app = embeddedServer(Netty, port = konfig.server.port) {
            devRapid()
        }.apply {
            start(wait = false)
        }
        logger.info { "Started ktor" }

        Runtime.getRuntime().addShutdownHook(Thread {
            app.stop(200, 30000)
        })
    }
}