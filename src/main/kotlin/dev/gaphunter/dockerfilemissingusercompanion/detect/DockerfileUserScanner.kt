package dev.gaphunter.dockerfilemissingusercompanion.detect

import dev.gaphunter.dockerfilemissingusercompanion.model.MissingUserHit

/**
 * Plain-text line scanner for a Dockerfile's FINAL build stage (the
 * last `FROM` in the file -- the real stage a plain `docker build`
 * with no `--target` actually runs) -- flags when that stage never
 * ends up with a non-root `USER`. Docker runs a container as root by
 * default when `USER` is absent, documented explicitly by Docker's
 * own guide for the `USER` instruction as a real security risk
 * (container escape/privilege escalation if a volume mount is
 * misconfigured).
 *
 * **Reasons over real multi-stage structure**, same "final stage
 * only" principle as `dockerfile-unused-stage-companion`'s own stage
 * scanner (deliberately a separate standalone copy): a `USER`
 * instruction in an EARLIER stage never counts on its own -- only the
 * last stage in the file is what actually runs. **Except** when that
 * last stage's own `FROM` names a PREVIOUS stage from this same file
 * (`FROM base AS final`, not an external image) -- Docker inherits
 * the base stage's state, `USER` included, so the final stage is
 * checked from wherever that chain of named stages last set one.
 *
 * **v0.2 scope, stated honestly:** the LAST `USER` instruction found
 * in the (possibly inherited) final stage decides -- a stage that
 * switches back to `USER root`/`USER 0` after an earlier non-root
 * `USER` is still flagged, since that's the real effective user at
 * container start. Never validates that the referenced user actually
 * exists (`USER appuser` with no prior `RUN useradd appuser` is a
 * different problem, out of scope here). A `USER` instruction split
 * across lines with a trailing `\` is not recognized (single-line
 * match only) -- rare in practice since the instruction is short, but
 * a known gap.
 */
object DockerfileUserScanner {

    private val FROM_LINE = Regex("""^FROM\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val USER_LINE = Regex("""^USER\s+(\S+)\s*$""", RegexOption.IGNORE_CASE)
    private val ROOT_USERS = setOf("root", "0")

    fun scan(text: String): List<MissingUserHit> {
        val lines = text.lines()
        // Keyed by lowercased stage name -> the USER value in effect when
        // that stage ended (null = still root/never set). Lets a later
        // `FROM <name> AS ...` that reuses an earlier stage inherit its
        // state instead of resetting to "no USER seen" like an external
        // base image would.
        val stageUserAtEnd = mutableMapOf<String, String?>()
        var lastFromLineIndex = -1
        var currentUser: String? = null
        var currentStageName: String? = null

        fun closeCurrentStage() {
            currentStageName?.let { stageUserAtEnd[it.lowercase()] = currentUser }
        }

        lines.forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val fromParts = parseFromLine(trimmed)
            if (fromParts != null) {
                closeCurrentStage()
                lastFromLineIndex = index
                val (baseRef, stageName) = fromParts
                currentUser = stageUserAtEnd[baseRef.lowercase()] // present only when baseRef is an earlier stage in this file
                currentStageName = stageName
                return@forEachIndexed
            }

            val userMatch = USER_LINE.find(trimmed)
            if (userMatch != null) {
                currentUser = userMatch.groupValues[1]
            }
        }
        closeCurrentStage()

        if (lastFromLineIndex < 0) return emptyList() // not a real Dockerfile shape -- nothing to check

        val effectiveUser = currentUser
        val isRootOrMissing = effectiveUser == null || rootUserName(effectiveUser) in ROOT_USERS
        if (!isRootOrMissing) return emptyList()

        return listOf(MissingUserHit(lastFromLineIndex + 1))
    }

    /** Strips an optional `:group` suffix (`USER appuser:appgroup`) and lowercases -- only the user part decides root-ness. */
    private fun rootUserName(userValue: String): String = userValue.substringBefore(':').lowercase()

    /**
     * `FROM [--flag ...] <baseRef> [AS <stageName>]`. Leading `--platform=...`-style
     * flags are skipped to find the real base image/stage reference.
     */
    private fun parseFromLine(trimmed: String): Pair<String, String?>? {
        val rest = FROM_LINE.find(trimmed)?.groupValues?.get(1) ?: return null
        val tokens = rest.trim().split(Regex("""\s+"""))
        var i = 0
        while (i < tokens.size && tokens[i].startsWith("--")) i++
        if (i >= tokens.size) return null
        val baseRef = tokens[i]
        val stageName = if (i + 2 < tokens.size && tokens[i + 1].equals("AS", ignoreCase = true)) {
            tokens[i + 2]
        } else {
            null
        }
        return baseRef to stageName
    }
}
