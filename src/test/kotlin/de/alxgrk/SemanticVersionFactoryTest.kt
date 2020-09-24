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
import de.alxgrk.version.SemanticVersionFactory
import de.alxgrk.version.VersionFactory
import io.mockk.mockk
import io.mockk.mockkObject
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SemanticVersionFactoryTest {

    private lateinit var project: MavenProject

    @BeforeEach
    internal fun setUp() {
        project = mockk()
        mockkObject(GitService)
    }

    @Test
    fun `parse version`() {
        val semanticVersionFactory: VersionFactory = SemanticVersionFactory
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-0-g123-dirty"),
            Version(1, 0, 0, "", "", Suffix(0, "123", true))
        )
        assertEquals(
            semanticVersionFactory.createFromString("0.0.1-1-gfe17e7f"),
            Version(0, 0, 1, "", "", Suffix(1, "fe17e7f", false))
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-2-g123-dirty"),
            Version(1, 0, 0, "", "", Suffix(2, "123", true))
        )
        assertEquals(semanticVersionFactory.createFromString("1.0.0-0"), Version(1, 0, 0, "0", "", null))
        assertEquals(semanticVersionFactory.createFromString("0.0.4"), Version(0, 0, 4, "", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.2.3"), Version(1, 2, 3, "", "", null))
        assertEquals(semanticVersionFactory.createFromString("10.20.30"), Version(10, 20, 30, "", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.1.2-prerelease+meta"),
            Version(1, 1, 2, "prerelease", "meta", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.1.2+meta"), Version(1, 1, 2, "", "meta", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.1.2+meta-valid"),
            Version(1, 1, 2, "", "meta-valid", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.0.0-alpha"), Version(1, 0, 0, "alpha", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0-beta"), Version(1, 0, 0, "beta", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha.beta"),
            Version(1, 0, 0, "alpha.beta", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha.beta.1"),
            Version(1, 0, 0, "alpha.beta.1", "", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.0.0-alpha.1"), Version(1, 0, 0, "alpha.1", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha0.valid"),
            Version(1, 0, 0, "alpha0.valid", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha.0valid"),
            Version(1, 0, 0, "alpha.0valid", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"),
            Version(1, 0, 0, "alpha-a.b-c-somethinglong", "build.1-aef.1-its-okay", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-rc.1+build.1"),
            Version(1, 0, 0, "rc.1", "build.1", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("2.0.0-rc.1+build.123"),
            Version(2, 0, 0, "rc.1", "build.123", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.2.3-beta"), Version(1, 2, 3, "beta", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("10.2.3-DEV-SNAPSHOT"),
            Version(10, 2, 3, "DEV-SNAPSHOT", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.2.3-SNAPSHOT-123"),
            Version(1, 2, 3, "SNAPSHOT-123", "", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.0.0"), Version(1, 0, 0, "", "", null))
        assertEquals(semanticVersionFactory.createFromString("2.0.0"), Version(2, 0, 0, "", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.1.7"), Version(1, 1, 7, "", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("2.0.0+build.1848"),
            Version(2, 0, 0, "", "build.1848", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("2.0.1-alpha.1227"),
            Version(2, 0, 1, "alpha.1227", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-alpha+beta"),
            Version(1, 0, 0, "alpha", "beta", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.2.3----RC-SNAPSHOT.12.9.1--.12+788"),
            Version(1, 2, 3, "---RC-SNAPSHOT.12.9.1--.12", "788", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.2.3----R-S.12.9.1--.12+meta"),
            Version(1, 2, 3, "---R-S.12.9.1--.12", "meta", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.2.3----RC-SNAPSHOT.12.9.1--.12"),
            Version(1, 2, 3, "---RC-SNAPSHOT.12.9.1--.12", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0+0.build.1-rc.10000aaa-kk-0.1"),
            Version(1, 0, 0, "", "0.build.1-rc.10000aaa-kk-0.1", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("9999999.9999999.9999999"),
            Version(9999999, 9999999, 9999999, "", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-0A.is.legal"),
            Version(1, 0, 0, "0A.is.legal", "", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.1.1"), Version(1, 1, 1, "", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-5-g5242341-dirty"),
            Version(1, 0, 0, "", "", Suffix(5, "5242341", true))
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-5-g5242341"),
            Version(1, 0, 0, "", "", Suffix(5, "5242341", false))
        )
        assertEquals(
            semanticVersionFactory.createFromString("5000.1.1000000-5-g5242341-dirty"),
            Version(5000, 1, 1000000, "", "", Suffix(5, "5242341", true))
        )
        assertEquals(semanticVersionFactory.createFromString("v1.0.0"), Version(1, 0, 0, "", "", null))
        assertEquals(semanticVersionFactory.createFromString("V1.0.0"), Version(1, 0, 0, "", "", null))
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0--dirty-5-g5242341"),
            Version(1, 0, 0, "-dirty", "", Suffix(5, "5242341", false))
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-dirty--5-g5242341"),
            Version(1, 0, 0, "dirty-", "", Suffix(5, "5242341", false))
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0-dirty-5--g5242341"),
            Version(1, 0, 0, "dirty-5--g5242341", "", null)
        )
        assertEquals(
            semanticVersionFactory.createFromString("1.0.0--g5242341"),
            Version(1, 0, 0, "-g5242341", "", null)
        )
        assertEquals(semanticVersionFactory.createFromString("1.0.0-g"), Version(1, 0, 0, "g", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0--g"), Version(1, 0, 0, "-g", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0-g123-5"), Version(1, 0, 0, "g123-5", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0-5-dirty"), Version(1, 0, 0, "5-dirty", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0-5"), Version(1, 0, 0, "5", "", null))
        assertEquals(semanticVersionFactory.createFromString("1.0.0-5-g"), Version(1, 0, 0, "5-g", "", null))

        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2.3-0123")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2.3-0123.0123")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.1.2+.123")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("+invalid")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("-invalid")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("-invalid+invalid")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("-invalid.01")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha.beta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha.beta.1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha.1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha+beta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha_beta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("alpha..")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("beta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha_beta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("-")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha..")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha..1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha...1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha....1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha.....1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha......1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-alpha.......1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("01.1.1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.01.1")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.1.01")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2.3.DEV")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2-SNAPSHOT")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.2-RC-SNAPSHOT")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("-1.0.3-gamma+b7718")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("+justmeta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("9.8.7+meta+meta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("9.8.7-whatever+meta+meta")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("9999999.9999999.9999999----RC-SNAPSHOT.12.09.1--------------------------------..12")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.1.1 ")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("111")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("a.b.c")
        }
        assertThrows(RuntimeException::class.java) {
            semanticVersionFactory.createFromString("1.0.0-")
        }
    }
}