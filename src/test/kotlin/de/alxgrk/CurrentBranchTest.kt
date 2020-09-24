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

import de.alxgrk.RunnerResult.Success
import de.alxgrk.git.GitService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.*
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CurrentBranchTest {

    private lateinit var project: MavenProject

    @BeforeEach
    internal fun setUp() {
        project = mockk {
            every { basedir } returns File("foo")
        }
        mockkObject(GitService)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(GitService)
    }

    @Test
    fun `group of master should be master`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("master")
        Assertions.assertEquals(branch.group, "master")
    }

    @Test
    fun `group of develop should be develop`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("develop")
        Assertions.assertEquals(branch.group, "develop")
    }

    @Test
    fun `group of feature-test should be feature`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("feature/test")
        Assertions.assertEquals(branch.group, "feature")
    }

    @Test
    fun `group of feature-test-test should be feature`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("feature/test/test")
        Assertions.assertEquals(branch.group, "feature")
    }

    @Test
    fun `id of feature-test should be feature-test`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("feature/test")
        Assertions.assertEquals(branch.id, "feature-test")
    }

    @Test
    fun `id of feature-test_a! should be feature-test_a!`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("feature/test_a!")
        Assertions.assertEquals(branch.id, "feature-test_a!")
    }

    @Test
    fun `id of feature-special-test should be feature-special-test`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("feature/special-test")
        Assertions.assertEquals(branch.id, "feature-special-test")
    }

    @Test
    fun `branch group of hotfix branch should be hotfix`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("hotfix/5.3.1")
        Assertions.assertEquals(branch.group, "hotfix")
    }

    @Test
    fun `branch group of release branch should be release`() {
        val branch = CurrentBranch(project)
        every { GitService.currentBranch(project = any()) } returns Success("release/5.3.0")
        Assertions.assertEquals(branch.group, "release")
    }
}