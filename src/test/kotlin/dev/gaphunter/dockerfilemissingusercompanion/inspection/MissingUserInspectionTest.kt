package dev.gaphunter.dockerfilemissingusercompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MissingUserInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(MissingUserInspection::class.java)
    }

    fun `test a Dockerfile with no USER produces a warning`() {
        myFixture.configureByText(
            "Dockerfile",
            """
            FROM node:20
            COPY . .
            CMD ["node", "index.js"]
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("non-root USER") == true })
    }

    fun `test a Dockerfile with a real non-root USER produces no warning`() {
        myFixture.configureByText(
            "Dockerfile",
            """
            FROM node:20
            RUN useradd appuser
            USER appuser
            CMD ["node", "index.js"]
            """.trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("non-root USER") == true })
    }

    fun `test a non-Dockerfile file is never scanned`() {
        myFixture.configureByText(
            "notes.txt",
            "FROM node:20\nCMD [\"node\", \"index.js\"]",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("non-root USER") == true })
    }
}
