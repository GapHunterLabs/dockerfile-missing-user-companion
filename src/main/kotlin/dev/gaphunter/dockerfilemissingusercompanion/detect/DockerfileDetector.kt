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

    /**
     * A trailing extension here means the file is prose/documentation
     * (a cheatsheet, a notes file) that just happens to start with
     * "dockerfile." -- not a variant Dockerfile anyone actually builds
     * (real variants look like `Dockerfile.prod`/`Dockerfile.arm64`,
     * never `Dockerfile.md`). Confirmed against a real-world corpus:
     * ~10% of GitHub files matching `dockerfile.*` by name are `.md`
     * cheatsheets with unrelated, often non-buildable `FROM` lines used
     * as documentation examples.
     */
    private val DOCUMENTATION_SUFFIXES = setOf("md", "markdown", "mdx", "rst", "adoc")

    fun isDockerfile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        if (lower in EXACT_NAMES) return true
        if (lower.endsWith(".dockerfile")) return true
        if (lower.startsWith("dockerfile.")) {
            return lower.substringAfterLast('.') !in DOCUMENTATION_SUFFIXES
        }
        return false
    }
}
