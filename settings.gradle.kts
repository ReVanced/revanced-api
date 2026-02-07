rootProject.name = "revanced-api"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/revanced/revanced-api")
            credentials(PasswordCredentials::class)
        }
    }
}
