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
import de.alxgrk.git.GitService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfoTest {

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
    fun `get dirty`() {
        val info = info()
        every { GitService.isDirty(project = any()) } returns true
        Assertions.assertEquals(true, info.dirty)
    }

    @Test
    fun `get last tag`() {
        val info = info()
        every { GitService.lastTag(project = any(), tagPrefix = any()) } returns Success("0.1.0")
        Assertions.assertEquals("0.1.0", info.lastTag)
    }

    @Test
    fun `get current tag`() {
        val info = info()
        every { GitService.currentTag(project = any()) } returns Success("0.1.0")
        Assertions.assertEquals("0.1.0", info.tag)
    }

    @Test
    fun `get last tag with prefix`() {
        val info = info("prj_")
        every { GitService.lastTag(project = any(), tagPrefix = any()) } returns Success("prj_0.1.0")
        Assertions.assertEquals("prj_0.1.0", info.lastTag)
    }

    @Test
    fun `get current tag with prefix`() {
        val info = info("prj_")
        every { GitService.currentTag(project = any()) } returns Success("prj_0.1.0")
        Assertions.assertEquals("prj_0.1.0", info.tag)
    }

    @Test
    fun `get short commit`() {
        val info = info()
        every { GitService.currentShortCommit(project = any()) } returns Success("1234567")
        Assertions.assertEquals("1234567", info.shortCommit)
    }

    @Test
    fun `get commit`() {
        val info = info()
        every { GitService.currentCommit(project = any()) } returns Success("123456789")
        Assertions.assertEquals("123456789", info.commit)
    }

    @Test
    fun `get branch`() {
        val info = info()
        Assertions.assertEquals(CurrentBranch(project), info.currentBranch)
    }

    @Test
    fun `get version`() {
        val info = info()
        every { GitService.currentTag(project = any()) } returns Success("0.1.0")
        Assertions.assertEquals(
            "Version(major=0, minor=1, patch=0, prerelease=, build=, suffix=null)",
            info.version.toString()
        )
    }

    @Test
    fun `current version is tagged with tag prefix`() {
        val info = info("prj_")
        every { GitService.currentTag(project = any(), tagPrefix = any()) } returns Success("prj_0.1.0")
        Assertions.assertEquals(
            "Version(major=0, minor=1, patch=0, prerelease=, build=, suffix=null)",
            info.version.toString()
        )
    }

    @Test
    fun `current version has no tag with tag prefix`() {
        val info = info("prj_")
        every { GitService.currentBranch(project = any()) } returns Success("foo")
        every { GitService.currentTag(project = any(), tagPrefix = any()) } returns Failure("error")
        every { GitService.lastTag(project = any(), tagPrefix = any()) } returns Success("prj_0.1.0")
        Assertions.assertEquals(
            "Version(major=0, minor=2, patch=0, prerelease=, build=, suffix=null)",
            info.version.toString()
        )
    }

    @Test
    fun `current version has not tag with tag prefix`() {
        val info = info()
        every { GitService.currentTag(project = any()) } returns Success("prj_0.1.0")
        assertThrows(RuntimeException::class.java) {
            info.version
        }
    }

    @Test
    fun `last version has not tag with tag prefix`() {
        val info = info()
        every { GitService.currentTag(project = any(), tagPrefix = any()) } returns Failure("error")
        every { GitService.lastTag(project = any(), tagPrefix = any()) } returns Success("prj_0.1.0")
        assertThrows(RuntimeException::class.java) {
            info.version
        }
    }

    private fun info(tagPrefix: String = TAG_PREFIX_DEFAULT) = Info(
        project,
        mockk(),
        tagPrefix,
        INITIAL_VERSION_DEFAULT,
        SNAPSHOT_SUFFIX_DEFAULT,
        DIRTY_MARKER_DEFAULT
    )
}
