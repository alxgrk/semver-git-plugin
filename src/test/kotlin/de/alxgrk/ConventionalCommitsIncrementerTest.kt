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

import de.alxgrk.git.GitCommandRunner
import de.alxgrk.incrementer.Incrementers
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConventionalCommitsIncrementerTest {

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
    fun `patch should be increased by 1`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns RunnerResult.Success(
            "7356414 fix: update gradle plugin publish due to security bugs\n" +
                    "45f65f6 fix: update version an changelog\n" +
                    "67f03b1 feat: Merge pull request #18 from ilovemilk/feature/support-multi-module\n" +
                    "fba5872 fix: add default tagPrefix behaviour\n" +
                    "2d03c4b fix: Merge pull request #17 from jgindin/support-multi-module\n" +
                    "f96697f fix: Merge remote-tracking branch 'origin/feature/add-more-tests' into develop\n" +
                    "73fc8b4 fix: Add support for multi-module projects.\n" +
                    "74e3eb1 fix: add test for kebab-case with numbers\n" +
                    "63ca60f fix: add more tests"
        )

        val versionIncrementerByName = Incrementers.getVersionIncrementerByName("CONVENTIONAL_COMMITS_INCREMENTER")
        require(versionIncrementerByName is IncrementerResult.Found)
        assertEquals(
            versionIncrementerByName.incrementer
                .increment(Version(0, 0, 0, "", "", null), project), Version(0, 1, 0, "", "", null)
        )
    }

    @Test
    fun `minor should be increased by 1`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns RunnerResult.Success(
            "7356414 fix: update gradle plugin publish due to security bugs\n" +
                    "45f65f6 fix: update version an changelog\n" +
                    "67f03b1 fix: Merge pull request #18 from ilovemilk/feature/support-multi-module\n" +
                    "fba5872 fix: add default tagPrefix behaviour\n" +
                    "2d03c4b fix: Merge pull request #17 from jgindin/support-multi-module\n" +
                    "f96697f fix: Merge remote-tracking branch 'origin/feature/add-more-tests' into develop\n" +
                    "73fc8b4 fix: Add support for multi-module projects.\n" +
                    "74e3eb1 fix: add test for kebab-case with numbers\n" +
                    "63ca60f fix: add more tests"
        )

        val versionIncrementerByName = Incrementers.getVersionIncrementerByName("CONVENTIONAL_COMMITS_INCREMENTER")
        require(versionIncrementerByName is IncrementerResult.Found)
        assertEquals(
            versionIncrementerByName.incrementer
                .increment(Version(0, 0, 0, "", "", null), project), Version(0, 0, 1, "", "", null)
        )
    }

    @Test
    fun `major should be increased by 1`() {
        every { GitCommandRunner.execute(projectDir = any(), args = any()) } returns RunnerResult.Success(
            "7356414 fix: update gradle plugin publish due to security bugs\n" +
                    "45f65f6 BREAKING CHANGE: update version an changelog\n" +
                    "67f03b1 fix: Merge pull request #18 from ilovemilk/feature/support-multi-module\n" +
                    "fba5872 feat: add default tagPrefix behaviour\n" +
                    "2d03c4b fix: Merge pull request #17 from jgindin/support-multi-module\n" +
                    "f96697f feat: Merge remote-tracking branch 'origin/feature/add-more-tests' into develop\n" +
                    "73fc8b4 fix: Add support for multi-module projects.\n" +
                    "74e3eb1 feat: add test for kebab-case with numbers\n" +
                    "63ca60f fix: add more tests"
        )

        val versionIncrementerByName = Incrementers.getVersionIncrementerByName("CONVENTIONAL_COMMITS_INCREMENTER")
        require(versionIncrementerByName is IncrementerResult.Found)
        assertEquals(
            versionIncrementerByName.incrementer
                .increment(Version(0, 0, 0, "", "", null), project), Version(1, 0, 0, "", "", null)
        )
    }
}
