package no.nav.nada.devrapid

import no.nav.nada.devrapid.schema.DevEvent
import no.nav.nada.devrapid.schema.NadaResourceNames
import no.nav.nada.devrapid.schema.Target

object GithubTranslator {

    fun translate(webHookData: DeploymentWebHook): DevEvent {
        val repoName = webHookData.repository?.name ?: webHookData.deployment.repositoryUrl.substringAfterLast("/")
        return DevEvent(
            nrn = NadaResourceNames(id = "nrn:github:$repoName:deploy:${webHookData.deployment.id}"),
            application = repoName,
            target = targetFromEnvironment(webHookData.deployment.environment),
            timestamp = webHookData.deployment.created_at.format(ISO_FORMATTER),
            team = "",
            additionalData = mapOf(
                "originalUrl" to webHookData.deployment.url
            )
        )
    }

    private fun targetFromEnvironment(environment: String): Target {
        val (envAndZone, namespace) = environment.split(":")
        val (env, zone) = envAndZone.split("-")
        return Target(namespace = namespace, zone = zone, environment = env)
    }
}