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

import de.alxgrk.git.GitService
import de.alxgrk.incrementer.MajorVersionIncrementer
import de.alxgrk.incrementer.MinorVersionIncrementer
import de.alxgrk.incrementer.NoVersionIncrementer
import de.alxgrk.incrementer.PatchVersionIncrementer
import io.mockk.mockk
import io.mockk.mockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IncrementerTest {

    private lateinit var project: MavenProject

    @BeforeEach
    internal fun setUp() {
        project = mockk()
        mockkObject(GitService)
    }

    @Test
    fun `bump major version`() {
        val version = MajorVersionIncrementer().increment(Version(1, 1, 1, "", "", null), project)
        Assertions.assertEquals(version, Version(2, 0, 0, "", "", null))
    }

    @Test
    fun `bump minor version`() {
        val version = MinorVersionIncrementer().increment(Version(1, 1, 1, "", "", null), project)
        Assertions.assertEquals(version, Version(1, 2, 0, "", "", null))
    }

    @Test
    fun `bump patch version`() {
        val version = PatchVersionIncrementer().increment(Version(1, 1, 1, "", "", null), project)
        Assertions.assertEquals(version, Version(1, 1, 2, "", "", null))
    }

    @Test
    fun `don't bump version`() {
        val version = NoVersionIncrementer().increment(Version(1, 1, 1, "", "", null), project)
        Assertions.assertEquals(version, Version(1, 1, 1, "", "", null))
    }
}
