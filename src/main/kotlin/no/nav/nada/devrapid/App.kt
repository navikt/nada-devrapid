package no.nav.nada.devrapid

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import java.time.Duration

val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    runBlocking {
        val konfig = Configuration()
        val producer = KafkaProducer<GenericRecord, GenericRecord>(konfig.devRapid.toProducerProps())
        logger.info { "Producer initialized" }
        val messageSender = KafkaMessageSender(konfig.devRapid.topic, producer)
        logger.info { "Sender ready" }
        val app = embeddedServer(Netty, port = konfig.server.port) {
            devRapid(
                messageSender = messageSender
            )
        }.apply {
            start(wait = false)
        }
        logger.info { "Started ktor" }

        Runtime.getRuntime().addShutdownHook(Thread {
            producer.close(Duration.ofSeconds(30))
            app.stop(200, 30000)
        })
    }
}