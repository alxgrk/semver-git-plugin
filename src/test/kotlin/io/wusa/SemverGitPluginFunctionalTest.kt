package io.wusa

import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SemverGitPluginFunctionalTest {

    @Test
    fun testDefaults() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }
        """)
        initializeGit(testProjectDirectory)
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.0.1"))
    }

    @Test
    fun testPatchReleaseWithConfiguration() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'patch'
            }
        """)
        initializeGit(testProjectDirectory)
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.0.1"))
    }

    @Test
    fun testMinorReleaseWithConfiguration() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'minor'
            }
        """)
        initializeGit(testProjectDirectory, "0.1.0")
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.1.0"))
    }

    @Test
    fun testMajorReleaseWithConfiguration() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'major'
            }
        """)
        initializeGit(testProjectDirectory, "1.0.0")
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 1.0.0"))
    }

    @Test
    fun testPatchBump() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'patch'
            }
        """)
        val git = initializeGit(testProjectDirectory)
        git.commit().setMessage("").call()
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.0.2-1-g"))
    }

    @Test
    fun testMinorBump() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'minor'
            }
        """)
        val git = initializeGit(testProjectDirectory)
        git.commit().setMessage("").call()
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.1.0-1-g"))
    }

    @Test
    fun testMajorBump() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'major'
            }
        """)
        val git = initializeGit(testProjectDirectory)
        git.commit().setMessage("").call()
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 1.0.0-1-g"))
    }

    @Test
    fun testNoneBump() {
        val testProjectDirectory = createTempDir()
        val buildFile = File(testProjectDirectory, "build.gradle")
        buildFile.writeText("""
            plugins {
                id 'io.wusa.semver-git-plugin'
            }

            semver {
                snapshotSuffix = '<count>-g<sha>'
                nextVersion = 'none'
            }
        """)
        val git = initializeGit(testProjectDirectory)
        git.commit().setMessage("").call()
        val result = GradleRunner.create()
                .withProjectDir(testProjectDirectory)
                .withArguments("showVersion")
                .withPluginClasspath()
                .build()
        assertTrue(result.output.contains("Version: 0.0.1-1-g"))
    }

    private fun initializeGit(directory: File, tag: String = "0.0.1"): Git {
        val git = Git.init().setDirectory(directory).call()
        val commit = git.commit().setMessage("").call()
        git.tag().setName(tag).setObjectId(commit).call()
        return git
    }
}