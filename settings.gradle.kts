rootProject.name = "revanced-api"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}
