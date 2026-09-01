package dev.gaphunter.dockerfilemissingusercompanion.detect

import dev.gaphunter.dockerfilemissingusercompanion.model.MissingUserHit

/**
 * Plain-text line scanner for a Dockerfile's FINAL build stage (the
 * last `FROM` in the file -- the real stage a plain `docker build`
 * with no `--target` actually runs) -- flags when that stage never
 * sets a non-root `USER` before its end. Docker runs a container as
 * root by default when `USER` is absent, documented explicitly by
 * Docker's own guide for the `USER` instruction as a real security
 * risk (container escape/privilege escalation if a volume mount is
 * misconfigured).
 *
 * **Reasons over real multi-stage structure**, same "final stage
 * only" principle as `dockerfile-unused-stage-companion`'s own stage
 * scanner (deliberately a separate standalone copy): a `USER`
 * instruction in an EARLIER stage never counts -- only the last stage
 * in the file is what actually runs.
 *
 * **v0.1 scope, stated honestly:** only detects total absence of a
 * non-root `USER` in the final stage (the LAST `USER` instruction
 * found in that stage decides -- a stage that switches back to
 * `USER root`/`USER 0` after an earlier non-root `USER` is still
 * flagged, since that's the real effective user at container start).
 * Never validates that the referenced user actually exists (`USER
 * appuser` with no prior `RUN useradd appuser` is a different problem,
 * out of scope here).
 */
object DockerfileUserScanner {

    private val FROM_LINE = Regex("""^FROM\s+""", RegexOption.IGNORE_CASE)
    private val USER_LINE = Regex("""^USER\s+(\S+)\s*$""", RegexOption.IGNORE_CASE)
    private val ROOT_USERS = setOf("root", "0")

    fun scan(text: String): List<MissingUserHit> {
        val lines = text.lines()
        var lastFromLineIndex = -1
        var lastUserValueInFinalStage: String? = null

        lines.forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            if (FROM_LINE.containsMatchIn(trimmed)) {
                lastFromLineIndex = index
                lastUserValueInFinalStage = null // a new stage starts -- forget any USER seen in a PRIOR stage
                return@forEachIndexed
            }

            val userMatch = USER_LINE.find(trimmed)
            if (userMatch != null) {
                lastUserValueInFinalStage = userMatch.groupValues[1]
            }
        }

        if (lastFromLineIndex < 0) return emptyList() // not a real Dockerfile shape -- nothing to check

        val effectiveUser = lastUserValueInFinalStage
        val isRootOrMissing = effectiveUser == null || rootUserName(effectiveUser) in ROOT_USERS
        if (!isRootOrMissing) return emptyList()

        return listOf(MissingUserHit(lastFromLineIndex + 1))
    }

    /** Strips an optional `:group` suffix (`USER appuser:appgroup`) and lowercases -- only the user part decides root-ness. */
    private fun rootUserName(userValue: String): String = userValue.substringBefore(':').lowercase()
}
