package no.nav.nada.devrapid

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding

private val local = ConfigurationMap(
    mapOf(
        "application.port" to "9090"
    )
)

private val dev = ConfigurationMap(
    mapOf(
        "application.port" to "9090"
    )
)
private val prod = ConfigurationMap(
    mapOf(
        "application.port" to "9090"
    )
)
data class Application(val port: Int = config()[Key("application.port", intType)])
data class Configuration(val application: Application = Application())

fun config() = when (System.getenv("NAIS_CLUSTER_NAME")) {
    "dev-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding dev
    "prod-gcp" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prod
    else -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding local
}