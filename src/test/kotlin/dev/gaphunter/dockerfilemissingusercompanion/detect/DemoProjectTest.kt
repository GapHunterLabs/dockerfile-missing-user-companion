package dev.gaphunter.dockerfilemissingusercompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The demo fixtures as a tester actually opens them: read from `demo/`
 * on disk, so the walkthrough in `demo/README.md` cannot drift away
 * from what the scanner does.
 */
class DemoProjectTest {

    private fun demoText(name: String) = File("demo/$name").readText()

    @Test
    fun `Dockerfile is flagged`() {
        val hits = DockerfileUserScanner.scan(demoText("Dockerfile"))
        assertEquals(1, hits.size)
    }

    @Test
    fun `Dockerfile safe is not flagged`() {
        assertTrue(DockerfileUserScanner.scan(demoText("Dockerfile.safe")).isEmpty())
    }

    @Test
    fun `Dockerfile multistage-shared-user is not flagged`() {
        assertTrue(DockerfileUserScanner.scan(demoText("Dockerfile.multistage-shared-user")).isEmpty())
    }
}
