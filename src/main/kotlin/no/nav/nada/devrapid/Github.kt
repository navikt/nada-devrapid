package no.nav.nada.devrapid

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import kotlinx.coroutines.launch
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import no.nav.nada.devrapid.schema.DevEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV")

@Serializer(forClass = ZonedDateTime::class)
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override fun serialize(encoder: Encoder, obj: ZonedDateTime) {
        encoder.encodeString(obj.format(ISO_FORMATTER))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString(), ISO_FORMATTER)
    }
}

@Serializable
data class DeploymentWebHook(val deployment: Deployment, val repository: Repository?)

@Serializable
data class Repository(
    val id: Long,
    val name: String,
    val full_name: String,
    val private: Boolean,
    val owner: User,
    val description: String?,
    @Serializable(with = ZonedDateTimeSerializer::class) val created_at: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class) val updated_at: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class) val pushed_at: ZonedDateTime
)

@Serializable
data class User(val login: String, val id: Long, val type: String)

@Serializable
data class Deployment(
    val url: String,
    val id: String,
    val sha: String,
    val ref: String,
    val task: String,
    val original_environment: String,
    val environment: String,
    val description: String?,
    val creator: User,
    @SerialName("repository_url") val repositoryUrl: String,
    @Serializable(with = ZonedDateTimeSerializer::class) val created_at: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class) val updated_at: ZonedDateTime
)

internal fun Routing.github(messageSender: MessageSender) {
    post("/github/deploy") {
        val githubDeploy = call.receive<DeploymentWebHook>()
        launch {
            val devEvent: DevEvent = GithubTranslator.translate(githubDeploy)
            messageSender.sendDevEvent(devEvent)
        }
        call.respond(HttpStatusCode.Accepted, "OK")
    }
}