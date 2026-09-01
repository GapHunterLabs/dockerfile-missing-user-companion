package dev.gaphunter.dockerfilemissingusercompanion.detect

/**
 * Decides whether a file is a Dockerfile, by name only -- same
 * detector as `dockerfile-layer-size-companion`/`dockerfile-unused-stage-companion`'s
 * own `DockerfileDetector` (deliberately conservative, opt-in-by-name
 * only, kept as a separate standalone copy so each plugin stays
 * independently installable).
 */
object DockerfileDetector {

    private val EXACT_NAMES = setOf("dockerfile")

    fun isDockerfile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        if (lower in EXACT_NAMES) return true
        if (lower.endsWith(".dockerfile")) return true
        if (lower.startsWith("dockerfile.")) return true
        return false
    }
}
