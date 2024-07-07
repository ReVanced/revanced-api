plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serilization)
    `maven-publish`
    signing
}

group = "app.revanced"

tasks {
    processResources {
        expand("projectVersion" to project.version)
    }

    // Used by gradle-semantic-release-plugin.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435.
    publish {
        dependsOn(shadowJar)
    }

    shadowJar {
        // Needed for Jetty to work.
        mergeServiceFiles()
    }
}

application {
    mainClass.set("app.revanced.api.command.MainCommandKt")
}

ktor {
    fatJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
    maven {
        // A repository must be specified for some reason. "registry" is a dummy.
        url = uri("https://maven.pkg.github.com/revanced/registry")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.jetty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.ktor)
    implementation(libs.kompendium.core)
    implementation(libs.h2)
    implementation(libs.logback.classic)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.dotenv.kotlin)
    implementation(libs.ktoml.core)
    implementation(libs.ktoml.file)
    implementation(libs.picocli)
    implementation(libs.kotlinx.datetime)
    implementation(libs.revanced.patcher)
    implementation(libs.revanced.library)
    implementation(libs.caffeine)
    implementation(libs.bouncy.castle.provider)
    implementation(libs.bouncy.castle.pgp)
}

// The maven-publish plugin is necessary to make signing work.
publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("revanced-api-publication") {
            from(components["java"])
        }
    }
}

signing {
    useGpgCmd()

    sign(publishing.publications["revanced-api-publication"])
}
