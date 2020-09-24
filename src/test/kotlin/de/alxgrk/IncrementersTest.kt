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

import de.alxgrk.incrementer.Incrementers
import io.mockk.mockk
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class IncrementersTest {

    private lateinit var project: MavenProject

    @BeforeEach
    internal fun setUp() {
        project = mockk()
    }

    @Test
    fun `no increment`() {
        val result = Incrementers.getVersionIncrementerByName("NO_VERSION_INCREMENTER")
        require(result is IncrementerResult.Found)
        assertEquals(
            result.incrementer.increment(Version(0, 0, 0, "", "", null), project),
            Version(0, 0, 0, "", "", null)
        )
    }

    @Test
    fun `patch increment`() {
        val result = Incrementers.getVersionIncrementerByName("PATCH_INCREMENTER")
        require(result is IncrementerResult.Found)
        assertEquals(
            result.incrementer.increment(Version(0, 0, 0, "", "", null), project), Version(0, 0, 1, "", "", null)
        )
    }

    @Test
    fun `minor increment`() {
        val result = Incrementers.getVersionIncrementerByName("MINOR_INCREMENTER")
        require(result is IncrementerResult.Found)
        assertEquals(
            result.incrementer.increment(Version(0, 0, 0, "", "", null), project), Version(0, 1, 0, "", "", null)
        )
    }

    @Test
    fun `major increment`() {
        val result = Incrementers.getVersionIncrementerByName("MAJOR_INCREMENTER")
        require(result is IncrementerResult.Found)
        assertEquals(
            result.incrementer.increment(Version(0, 0, 0, "", "", null), project), Version(1, 0, 0, "", "", null)
        )
    }

    @Test
    fun `wrong incrementer`() {
        val result = Incrementers.getVersionIncrementerByName("WRONG_INCREMENTER")
        assertTrue(result is IncrementerResult.NoIncrementerFound)
    }
}
