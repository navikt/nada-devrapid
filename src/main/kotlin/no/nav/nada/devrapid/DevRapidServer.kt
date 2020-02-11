package no.nav.nada.devrapid

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.serialization
import io.ktor.util.pipeline.PipelineContext
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import kotlinx.serialization.MissingFieldException
import no.nav.nada.devrapid.health.health

fun Application.devRapid(
    messageSender: MessageSender
) {
    install(DefaultHeaders)
    install(MicrometerMetrics) {
        registry =
            PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)
    }
    install(ContentNegotiation) {
        serialization()
    }
    install(StatusPages) {
        exception<IllegalStateException> { cause ->
            badRequest(cause)
        }
        exception<MissingFieldException> { cause ->
            badRequest(cause)
        }
    }
    routing {
        health()
        github(messageSender)
        devRapidRoute(messageSender)
    }
}

private suspend fun <T : Throwable> PipelineContext<Unit, ApplicationCall>.badRequest(
    cause: T
) {
    call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
}