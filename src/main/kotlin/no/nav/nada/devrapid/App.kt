package no.nav.nada.devrapid

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

val logger = KotlinLogging.logger {}
fun main(args: Array<String>) {
    runBlocking {
        Server.startServer(getPort("PORT", 9090)).start(false)
    }
}

fun getEnv(envName: String): String? {
    return System.getenv(envName)
}

fun getPort(envName: String, default: Int): Int {
    return getEnv(envName)?.toInt() ?: default
}