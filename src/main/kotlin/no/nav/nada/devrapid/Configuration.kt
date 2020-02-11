package no.nav.nada.devrapid

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.Properties
import java.util.UUID

private val local = ConfigurationMap(
    mapOf(
        "server.port" to "9090",
        "devrapid.bootstrap" to "localhost:9092",
        "devrapid.username" to "",
        "devrapid.password" to "",
        "devrapid.topic" to "aapen.nada.devrapid",
        "devrapid.bootstrap" to "localhost:9092",
        "devrapid.schemaregistry" to "http://localhost:8081"
    )
)

private val dev = ConfigurationMap(
    mapOf(
        "server.port" to "9090"
    )
)
private val prod = ConfigurationMap(
    mapOf(
        "server.port" to "9090"
    )
)
data class Server(val port: Int = config()[Key("server.port", intType)])
data class Configuration(val server: Server = Server(), val devRapid: DevRapid = DevRapid())
data class DevRapid(
    val bootstrapServer: String = config()[Key("devrapid.bootstrap", stringType)],
    val username: String = config()[Key("devrapid.username", stringType)],
    val password: String = config()[Key("devrapid.password", stringType)],
    val topic: String = config()[Key("devrapid.topic", stringType)],
    val schemaRegistry: String = config()[Key("devrapid.schemaregistry", stringType)]
) {
    fun toProducerProps(): Properties {
        return Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put(ProducerConfig.ACKS_CONFIG, "1")
            put(ProducerConfig.CLIENT_ID_CONFIG, "nada-devrapid-${UUID.randomUUID().toString().substring(0 until 16)}")
        }
    }
}
fun config() = when (System.getenv("NAIS_CLUSTER_NAME")) {
    "dev-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding dev
    "prod-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prod
    else -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding local
}