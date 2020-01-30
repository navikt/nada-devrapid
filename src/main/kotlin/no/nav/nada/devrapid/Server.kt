package no.nav.nada.devrapid

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.serialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import mu.KotlinLogging

object Server {
    val logger = KotlinLogging.logger {}
    suspend fun startServer(port: Int): NettyApplicationEngine {
        DefaultExports.initialize()
        logger.info { "Starting server on port $port" }
        return embeddedServer(Netty, port = port) {
            install(DefaultHeaders)
            install(MicrometerMetrics) {
                registry =
                    PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)
            }
            install(ContentNegotiation) {
                serialization()
            }
            routing {
                get("/prometheus") {
                    val names = call.request.queryParameters.getAll("name")?.toSet() ?: emptySet()
                    call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004), HttpStatusCode.OK) {
                        TextFormat.write004(this, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(names))
                    }
                }

                get("/isAlive") {
                    call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
                }

                get("/isReady") {
                    call.respondText(text = "READY", contentType = ContentType.Text.Plain)
                }
            }
        }
    }
}