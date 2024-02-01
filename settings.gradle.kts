rootProject.name = "app.revanced.revanced-api"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}