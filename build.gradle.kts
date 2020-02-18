import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.serialization") version Kotlin.version
    application
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}
val githubUser: String? by project
val githubPassword: String? by project
repositories {
    jcenter()
    mavenCentral()
    maven("http://packages.confluent.io/maven/")
    mavenLocal()
    maven {
        name = "nav-devrapid-schema"
        url = uri("https://maven.pkg.github.com/navikt/nada-devrapid-schema")
        credentials {
            username = githubUser ?: "x-access-token"
            password = githubPassword ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

configurations {
    "implementation" {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    "testImplementation" {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}
application {
    applicationName = "nada-devrapid"
    mainClassName = "no.nav.nada.devrapid.AppKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(Kotlin.Serialization.runtime)
    // Http Server
    implementation(Ktor.server("netty"))
    implementation(Ktor.metrics("micrometer"))
    implementation(Ktor.serialization)

    // Konfiguration
    implementation(Konfig.konfig)

    // Kafka
    implementation(Kafka.clients)
    implementation(Confluent.avroSerializer)
    implementation(Nada.devRapidSchema)
    implementation(Avro4k.core)

    // Logging
    implementation(Kotlin.Logging.kotlinLogging)
    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.Logstash.logstashLayout)

    // Prometheus / Metrics
    implementation(Prometheus.library("hotspot"))
    implementation(Prometheus.library("common"))
    implementation(Prometheus.library("log4j2"))
    implementation(Micrometer.prometheusRegistry)

    // Test
    testImplementation(JUnit.lib("api"))
    testImplementation(JUnit.lib("engine"))
    testImplementation(AssertJ.core)
    testImplementation(Ktor.server("test-host"))
}

configurations {
    this.all {
        exclude(group = "ch.qos.logback")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

spotless {
    kotlin {
        ktlint("0.33.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.33.0")
    }
}

tasks.named("shadowJar") {
    dependsOn("test")
}

tasks.named("jar") {
    dependsOn("test")
}

tasks.named("compileKotlin") {
    dependsOn("spotlessCheck")
}
tasks.named("spotlessCheck") {
    dependsOn("spotlessApply")
}
