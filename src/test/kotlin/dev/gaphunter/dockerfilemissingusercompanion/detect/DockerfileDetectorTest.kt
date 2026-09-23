package dev.gaphunter.dockerfilemissingusercompanion.detect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DockerfileDetectorTest {

    @Test
    fun `recognizes the exact name Dockerfile, any case`() {
        assertTrue(DockerfileDetector.isDockerfile("Dockerfile"))
        assertTrue(DockerfileDetector.isDockerfile("dockerfile"))
        assertTrue(DockerfileDetector.isDockerfile("DOCKERFILE"))
    }

    @Test
    fun `recognizes a dot-dockerfile suffix`() {
        assertTrue(DockerfileDetector.isDockerfile("app.dockerfile"))
    }

    @Test
    fun `recognizes a real variant like Dockerfile-dot-prod`() {
        assertTrue(DockerfileDetector.isDockerfile("Dockerfile.prod"))
        assertTrue(DockerfileDetector.isDockerfile("Dockerfile.arm64"))
    }

    @Test
    fun `does not treat a markdown cheatsheet named Dockerfile-dot-md as a real Dockerfile`() {
        assertFalse(DockerfileDetector.isDockerfile("Dockerfile.md"))
        assertFalse(DockerfileDetector.isDockerfile("dockerfile.md"))
        assertFalse(DockerfileDetector.isDockerfile("Dockerfile.markdown"))
    }

    @Test
    fun `does not treat unrelated files as Dockerfiles`() {
        assertFalse(DockerfileDetector.isDockerfile("docker-compose.yml"))
        assertFalse(DockerfileDetector.isDockerfile("notes.txt"))
    }
}
