package no.nav.nada.devrapid

import no.nav.nada.devrapid.schema.DevEvent
import no.nav.nada.devrapid.schema.NadaResourceNames
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import com.sksamuel.avro4k.Avro

class KafkaMessageSender(val topic: String, val kafkaProducer: KafkaProducer<GenericRecord, GenericRecord>) : MessageSender {
    override suspend fun sendDevEvent(devEvent: DevEvent) {
        val value = Avro.default.toRecord(DevEvent.serializer(), devEvent)
        val key = Avro.default.toRecord(NadaResourceNames.serializer(), devEvent.nrn)
        kafkaProducer.send(ProducerRecord(topic, key, value))
    }
}