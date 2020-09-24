/*
 * The MIT License
 * Copyright © 2019 Matthias Held
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.alxgrk

import de.alxgrk.RunnerResult.Failure
import de.alxgrk.RunnerResult.Success
import de.alxgrk.git.GitCommandRunner
import de.alxgrk.git.GitService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitServiceTest {

    private lateinit var project: MavenProject

    @BeforeEach
    internal fun setUp() {
        project = mockk {
            every { basedir } returns File("foo")
        }
        mockkObject(GitCommandRunner)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(GitCommandRunner)
    }

    @Test
    fun `git is dirty`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success("modified")
        assertEquals(true, GitService.isDirty(project))
    }

    @Test
    fun `git is not dirty`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertEquals(false, GitService.isDirty(project))
    }

    @Test
    fun `get last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success("modified")
        assertEquals("modified", GitService.lastTag(project).toString())
    }

    @Test
    fun `no last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertTrue(GitService.lastTag(project) is Failure)
    }

    @Test
    fun `get current tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success("modified")
        assertEquals("modified", GitService.currentTag(project).toString())
    }

    @Test
    fun `no current tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertTrue(GitService.currentTag(project) is Failure)
    }

    @Test
    fun `get commit sha`() {
        every {
            GitCommandRunner.execute(
                projectDir = any(),
                args = any()
            )
        } returns Success("5f68d6b1ba57fd183e2c0e6cb968c4353907fa17")
        assertEquals("5f68d6b1ba57fd183e2c0e6cb968c4353907fa17", GitService.currentCommit(project).toString())
    }

    @Test
    fun `no current commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertTrue(GitService.currentCommit(project) is Failure)
    }

    @Test
    fun `get short commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success("916776")
        assertEquals("916776", GitService.currentShortCommit(project).toString())
    }

    @Test
    fun `no current short commit sha`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertTrue(GitService.currentShortCommit(project) is Failure)
    }

    @Test
    fun `get current branch master`() {
        every {
            GitCommandRunner.execute(
                projectDir = any(),
                args = any()
            )
        } returns Success("* master 5824168c73ba0618c1b6e384fbd7d61c5e8b8bc3")
        assertEquals("master", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch feature-test`() {
        every {
            GitCommandRunner.execute(
                projectDir = any(),
                args = any()
            )
        } returns Success("* feature/test 5824168c73ba0618c1b6e384fbd7d61c5e8b8bc3")
        assertEquals("feature/test", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch feature-test with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/test                cd55642b18ef34d976eda337d2b7abd296b37c8f remove code quality\n" +
                    "  remotes/origin/feature/test cd55642b18ef34d976eda337d2b7abd296b37c8f remove code quality"
        )
        assertEquals("feature/test", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch feature-reactiveTests with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/reactiveTests           831965a6c57434276c70c8e1134244dd6077b1fc fix tests with timeout\n" +
                    "  hotfix/codePrefix                4ad7116a019553ff19d0e338bf7e602374d72c04 [behind 1] fixed publish code test for added prefix\n" +
                    "  remotes/origin/develop           13fc04d392d51d9bc7b70c8d52b2d8cd6cc1199a Merge branch 'feature/reactive-tests' into 'develop'\n" +
                    "  remotes/origin/hotfix/codePrefix a3b40aa8be599003c8656d5cc9a460ffd61fe1f9 escaping / in regex for branch detection\n"
        )
        assertEquals("feature/reactiveTests", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch hotfix-codePrefix with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* hotfix/codePrefix                4ad7116a019553ff19d0e338bf7e602374d72c04 [behind 1] fixed publish code test for added prefix\n" +
                    "  remotes/origin/hotfix/codePrefix a3b40aa8be599003c8656d5cc9a460ffd61fe1f9 escaping / in regex for branch detection"
        )
        assertEquals("hotfix/codePrefix", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch feature-s-version-3 with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/s-version-3                9ea487bb167c89a6453fd2e72740a492c6782887 use kebab case\n" +
                    "  remotes/origin/feature/s-version-3 9ea487bb167c89a6453fd2e72740a492c6782887 use kebab case"
        )
        assertEquals("feature/s-version-3", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch feature-abcd-10847-abcde-abc with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/abcd-10847-abcde-abc                9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/feature/abcd-10847-abcde-abc 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("feature/abcd-10847-abcde-abc", GitService.currentBranch(project).toString())
    }

    @Test
    fun `get current branch hotfix-5-3-1 with origin`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* hotfix/5.3.1              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/hotfix/5.3.1 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("hotfix/5.3.1", GitService.currentBranch(project).toString())
    }

    @Test
    fun `issue-35 fix branch regex`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/bellini/test-branch-version              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/feature/bellini/test-branch-version 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("feature/bellini/test-branch-version", GitService.currentBranch(project).toString())
    }

    @Test
    fun `camelCase branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/camelCase              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/feature/camelCase 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("feature/camelCase", GitService.currentBranch(project).toString())
    }

    @Test
    fun `UPPER_CASE branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/UPPER_CASE              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/feature/UPPER_CASE 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("feature/UPPER_CASE", GitService.currentBranch(project).toString())
    }

    @Test
    fun `PascalCase branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "* feature/PascalCase              9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc\n" +
                    "  remotes/origin/feature/PascalCase 9ea487bb167c89a6453fd2e72740a492c6782887 abcd-10847-abcde-abc"
        )
        assertEquals("feature/PascalCase", GitService.currentBranch(project).toString())
    }

    @Test
    fun `no current branch`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Failure("error")
        assertTrue(GitService.currentBranch(project) is Failure)
    }

    @Test
    fun `get list of all commits since last tag`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            "7356414 update gradle plugin publish due to security bugs\n" +
                    "45f65f6 update version an changelog\n" +
                    "67f03b1 Merge pull request #18 from ilovemilk/feature/support-multi-module\n" +
                    "fba5872 add default tagPrefix behaviour\n" +
                    "2d03c4b Merge pull request #17 from jgindin/support-multi-module\n" +
                    "f96697f Merge remote-tracking branch 'origin/feature/add-more-tests' into develop\n" +
                    "73fc8b4 Add support for multi-module projects.\n" +
                    "74e3eb1 add test for kebab-case with numbers\n" +
                    "63ca60f add more tests"
        )
        val listOfCommits = listOf(
            "7356414 update gradle plugin publish due to security bugs",
            "45f65f6 update version an changelog",
            "67f03b1 Merge pull request #18 from ilovemilk/feature/support-multi-module",
            "fba5872 add default tagPrefix behaviour",
            "2d03c4b Merge pull request #17 from jgindin/support-multi-module",
            "f96697f Merge remote-tracking branch 'origin/feature/add-more-tests' into develop",
            "73fc8b4 Add support for multi-module projects.",
            "74e3eb1 add test for kebab-case with numbers",
            "63ca60f add more tests"
        )
        assertEquals(GitService.getCommitsSinceLastTag(project), listOfCommits)
    }

    @Test
    fun `get ignored files`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns Success(
            """
                RM ../kotlin/io/wusa/VersionIncrementerTest.kt -> ../kotlin/de/alxgrk/IncrementersTest.kt
                RM ../kotlin/io/wusa/InfoTest.kt -> ../kotlin/de/alxgrk/InfoTest.kt
                RM ../kotlin/io/wusa/FunctionalBaseTest.kt -> ../kotlin/de/alxgrk/MojosIntegrationTest.kt
                RM ../kotlin/io/wusa/SemanticVersionFactoryTest.kt -> ../kotlin/de/alxgrk/SemanticVersionFactoryTest.kt
                D  ../kotlin/io/wusa/SemverGitPluginExtensionTest.kt
                D  ../kotlin/io/wusa/SemverGitPluginGroovyFunctionalTest.kt
                D  ../kotlin/io/wusa/SemverGitPluginKotlinFunctionalTest.kt
                ?? ../../../pom.xml
                ?? ../../../semver-git-plugin.iml
                ?? ../../main/kotlin/de/alxgrk/Data.kt
                ?? ../../main/kotlin/de/alxgrk/Mojos.kt
                ?? ../../../target/
                !! ../../../target/classes/de/
                !! ../../../target/test-classes/de/
            """.trimIndent()
        )
        assertEquals(
            listOf("../../../target/classes/de/", "../../../target/test-classes/de/"),
            GitService.getIgnoredFiles(project)
        )
    }
}
