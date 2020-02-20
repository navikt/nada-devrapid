package no.nav.nada.devrapid

import no.nav.nada.devrapid.schema.DevEvent
import no.nav.nada.devrapid.schema.NadaResourceNames
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import com.sksamuel.avro4k.Avro
import mu.KotlinLogging

class KafkaMessageSender(val topic: String, val kafkaProducer: KafkaProducer<GenericRecord, GenericRecord>) : MessageSender {
    companion object {
        val logger = KotlinLogging.logger {}
    }
    override suspend fun sendDevEvent(devEvent: DevEvent) {
        val value = Avro.default.toRecord(DevEvent.serializer(), devEvent)
        val key = Avro.default.toRecord(NadaResourceNames.serializer(), devEvent.nrn)
        kafkaProducer.send(ProducerRecord(topic, key, value)) { m, err ->
            if (err != null) {
                logger.warn { err }
            } else {
                logger.info { "Sent message with offset ${m.offset()} on partition ${m.partition()}" }
            }
        }
    }
}