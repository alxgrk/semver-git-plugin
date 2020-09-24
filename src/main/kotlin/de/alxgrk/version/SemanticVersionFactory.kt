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
package de.alxgrk.version

import de.alxgrk.Suffix
import de.alxgrk.Version

object SemanticVersionFactory : VersionFactory {

    private val suffixRegex = """(?:-(?<count>[0-9]+)(?:-g(?<sha>[0-9a-f]{1,7}))(?<dirty>-dirty)?)$""".toRegex()
    private val versionRegex =
        """^[vV]?(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+(?<build>[a-zA-Z0-9][a-zA-Z0-9\.-]+)?)?$""".toRegex()

    override fun createFromString(describe: String): Version {
        try {
            val suffix = parseSuffix(describe)
            val version = removeSuffixFromDescribe(describe, suffix)
            val parsedVersion = parseVersion(version)

            parsedVersion.suffix = suffix
            return parsedVersion
        } catch (ex: IllegalArgumentException) {
            throw RuntimeException("The last tag is not a semantic version: $describe.")
        }
    }

    private fun removeSuffixFromDescribe(describe: String, suffix: Suffix?): String {
        var version = describe
        if (suffix != null) {
            version = suffixRegex.replace(describe, "")
        }
        return version
    }

    private fun parseSuffix(describe: String): Suffix? {
        return suffixRegex.find(describe)
            ?.destructured
            ?.let { (count, sha, dirty) ->
                Suffix(count.toInt(), sha, dirty.isNotEmpty())
            }
    }

    private fun parseVersion(version: String): Version {
        return versionRegex.matchEntire(version)
            ?.destructured
            ?.let { (major, minor, patch, prerelease, build) ->
                Version(major.toInt(), minor.toInt(), patch.toInt(), prerelease, build, null)
            }
            ?: throw IllegalArgumentException("Bad input '$version'")
    }
}

interface VersionFactory {

    fun createFromString(describe: String): Version
}