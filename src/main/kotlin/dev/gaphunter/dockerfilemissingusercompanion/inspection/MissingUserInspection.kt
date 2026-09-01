package dev.gaphunter.dockerfilemissingusercompanion.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.dockerfilemissingusercompanion.detect.DockerfileDetector
import dev.gaphunter.dockerfilemissingusercompanion.detect.DockerfileUserScanner
import dev.gaphunter.dockerfilemissingusercompanion.review.ReviewPrompt

/**
 * Flags a Dockerfile whose final build stage never sets a non-root
 * `USER` -- Docker runs a container as root by default when `USER` is
 * absent, a real risk documented explicitly by Docker's own guide for
 * the instruction (container escape/privilege escalation if a volume
 * mount is misconfigured). Runs via [checkFile] (whole-file text scan)
 * -- see `build.gradle.kts` for why no Dockerfile-language PSI
 * dependency is taken.
 */
class MissingUserInspection : LocalInspectionTool() {

    companion object {
        const val MAX_FILE_LENGTH = 500_000
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val virtualFile = file.virtualFile ?: return null
        if (!DockerfileDetector.isDockerfile(virtualFile.name)) return null

        val text = file.text
        if (text.length > MAX_FILE_LENGTH) return null

        val hits = DockerfileUserScanner.scan(text)
        if (hits.isEmpty()) return null

        val document = file.viewProvider.document ?: return null
        val problems = mutableListOf<ProblemDescriptor>()

        for (hit in hits) {
            if (hit.anchorLineNumber - 1 !in 0 until document.lineCount) continue
            val lineStartOffset = document.getLineStartOffset(hit.anchorLineNumber - 1)
            val lineEndOffset = document.getLineEndOffset(hit.anchorLineNumber - 1)
            val anchor = leafElementAt(file, lineStartOffset) ?: continue
            val anchorStart = anchor.textRange.startOffset
            val relativeRange = TextRange(
                (lineStartOffset - anchorStart).coerceAtLeast(0),
                (lineEndOffset - anchorStart).coerceAtMost(anchor.textLength),
            )
            if (relativeRange.startOffset >= relativeRange.endOffset) continue

            problems += manager.createProblemDescriptor(
                anchor,
                relativeRange,
                "This stage never switches to a non-root USER -- the container runs as root by default, " +
                    "a real risk if a volume mount is misconfigured",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
            )

            ReviewPrompt.recordHit(file.project, "${virtualFile.path}:${hit.anchorLineNumber}")
        }

        return if (problems.isEmpty()) null else problems.toTypedArray()
    }

    private fun leafElementAt(file: PsiFile, startOffset: Int): PsiElement? {
        if (startOffset < 0 || startOffset >= file.textLength) return null
        var element = file.findElementAt(startOffset) ?: return file
        while (element.firstChild != null) {
            element = element.firstChild
        }
        return element
    }
}
