package no.nav.nada.devrapid

import com.sksamuel.avro4k.Avro
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import no.nav.nada.devrapid.schema.DevEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class GithubTests {
    val jsonSerialization = Json(JsonConfiguration.Stable.copy(strictMode = false))

    @Test
    fun `Can read a github deployment event`() {
        val json = "deploymentevent.json".fromFile()
        val deploymentWebHook = jsonSerialization.parse(DeploymentWebHook.serializer(), json)
        assertThat(deploymentWebHook.deployment.sha).isEqualTo("2d0812c093f25c80a94613aa9ba560480b4c60fa")
    }

    @Test
    fun `Can handle a posted github deployment event`() {
        val devEvent = "deploymentevent.json".fromFile()
        val messageSender: MessageSender = object : MessageSender {
            override suspend fun sendDevEvent(devEvent: DevEvent) {
                assertThat(devEvent.nrn.id).isEqualTo("nrn:github:unleash:deploy:200922047")
            }
        }
        withTestApplication(devRapidApi(messageSender)) {
            val status = handleRequest(HttpMethod.Post, "/github/deploy") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(devEvent)
            }.response.status()
            assertThat(status).isEqualTo(HttpStatusCode.Accepted)
        }
    }
    @Test
    fun `Can handle converting to GenericRecord from DevEvent`() {
        val devEvent = "deploymentevent.json".fromFile()
        val messageSender: MessageSender = object : MessageSender {
            override suspend fun sendDevEvent(devEvent: DevEvent) {
                val record = Avro.default.toRecord(DevEvent.serializer(), devEvent)
                assertThat(record.get("application")).isEqualTo("unleash")
            }
        }
        withTestApplication(devRapidApi(messageSender)) {
            val status = handleRequest(HttpMethod.Post, "/github/deploy") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(devEvent)
            }.response.status()
            assertThat(status).isEqualTo(HttpStatusCode.Accepted)
        }
    }

    @Test
    fun `Returns 400 with a proper error code if incorrect format`() {
        val event = "{}"
        val messageSender: MessageSender = object : MessageSender {
            override suspend fun sendDevEvent(devEvent: DevEvent) {
                assertThat(devEvent.nrn.id).isEqualTo("nrn:github:unleash:deploy:200922047")
            }
        }
        withTestApplication(devRapidApi(messageSender)) {
            val status = handleRequest(HttpMethod.Post, "/github/deploy") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(event)
            }.response.status()
            assertThat(status).isEqualTo(HttpStatusCode.BadRequest)
        }
    }
}

fun String.fromFile(): String {
    return GithubTests::class.java.getResource("/$this").readText(Charset.forName("UTF-8"))
}

internal fun devRapidApi(messageSender: MessageSender): Application.() -> Unit {
    return fun Application.() {
        devRapid(messageSender)
    }
}