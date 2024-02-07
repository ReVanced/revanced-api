plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serilization)
}

group = "app.revanced"

tasks {
    processResources {
        expand("projectVersion" to project.version)
    }

    /*
    Dummy task to hack gradle-semantic-release-plugin to release this project.

    Explanation:
    SemVer is a standard for versioning libraries.
    For that reason the semantic-release plugin uses the "publish" task to publish libraries.
    However, this subproject is not a library, and the "publish" task is not available for this subproject.
    Because semantic-release is not designed to handle this case, we need to hack it.

    RE: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
     */
    register<DefaultTask>("publish") {
        group = "publishing"
        description = "Dummy task to hack gradle-semantic-release-plugin to release ReVanced API"
        dependsOn(startShadowScripts)
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
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
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
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.ktor)
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

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
