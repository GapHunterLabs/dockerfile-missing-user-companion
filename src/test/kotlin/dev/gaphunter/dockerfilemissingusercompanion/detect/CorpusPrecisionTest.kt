package dev.gaphunter.dockerfilemissingusercompanion.detect

import org.junit.Test
import java.io.File

/**
 * Runs the scanner over a corpus of real Dockerfiles and writes every
 * hit, with its source repo, to build/corpus-verdicts.txt for manual
 * review. Skipped unless the DOCKERFILE_USER_CORPUS environment
 * variable points at a corpus directory (public files from other
 * repositories, one `Dockerfile` + `SOURCE.txt` per subdirectory,
 * never committed here).
 */
class CorpusPrecisionTest {

    @Test
    fun `hits on a real corpus`() {
        val corpus = System.getenv("DOCKERFILE_USER_CORPUS")?.let(::File)?.takeIf { it.isDirectory } ?: return
        val dirs = corpus.listFiles { f -> f.isDirectory }!!.sortedBy { it.name }
        val report = StringBuilder()
        var scanned = 0
        var flagged = 0
        for (dir in dirs) {
            val dockerfile = File(dir, "Dockerfile").takeIf { it.isFile } ?: continue
            val source = File(dir, "SOURCE.txt").takeIf { it.isFile }?.readText()?.trim() ?: dir.name
            scanned++
            val text = dockerfile.readText().replace("\r\n", "\n")
            val hits = DockerfileUserScanner.scan(text)
            if (hits.isEmpty()) continue
            flagged++
            for (hit in hits) {
                val lineText = text.lines().getOrElse(hit.anchorLineNumber - 1) { "" }.trim()
                report.append("$source:${hit.anchorLineNumber} | ${lineText.take(200)}\n")
            }
        }
        val summary = "$scanned Dockerfiles scanned, $flagged flagged"
        File("build").mkdirs()
        File("build/corpus-verdicts.txt").writeText(summary + "\n\n" + report)
        println(summary)
    }
}
