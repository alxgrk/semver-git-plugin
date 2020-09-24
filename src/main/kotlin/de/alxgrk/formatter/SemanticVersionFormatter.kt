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
package de.alxgrk.formatter

import de.alxgrk.*
import de.alxgrk.Branch.Companion.DEFAULT_FORMATTER_FUNCTION

object SemanticVersionFormatter {

    fun Info.format(branches: List<Branch>, snapshotSuffix: String, dirtyMarker: String): String {
        if (!hasFirstCommit()) return appendSuffix(buildVersionString(), snapshotSuffix)

        if (hasTag()) {
            return formatVersionWithTag()
        }

        val formattedVersion = formatVersionWithoutTag(branches, dirtyMarker)
        if (!hasTag()) {
            return appendSuffix(formattedVersion, snapshotSuffix)
        }
        return formattedVersion
    }

    private fun Info.formatVersionWithTag(): String {
        var versionString = buildVersionString()
        if (hasVersionPrerelease()) {
            versionString = appendPrerelease(versionString)
        }
        if (hasVersionBuildInformation()) {
            versionString = appendBuildInformation(versionString)
        }
        return versionString
    }

    private fun Info.appendBuildInformation(versionString: String): String {
        var versionString1 = versionString
        versionString1 += "+${version.build}"
        return versionString1
    }

    private fun Info.appendPrerelease(versionString: String): String {
        var versionString1 = versionString
        versionString1 += "-${version.prerelease}"
        return versionString1
    }

    private fun Info.buildVersionString() = "${version.major}.${version.minor}.${version.patch}"

    private fun Info.formatVersionWithoutTag(branches: List<Branch>, dirtyMarker: String): String {
        val regexFormatterPair = RegexResolver.findMatchingRegex(branches, currentBranch.name)
        var formattedVersion = DEFAULT_FORMATTER_FUNCTION(this)
        formattedVersion = appendDirtyMarker(formattedVersion, version.suffix, dirtyMarker)
        regexFormatterPair?.let {
            formattedVersion = regexFormatterPair.formatterFunction(this)
            formattedVersion = appendDirtyMarker(formattedVersion, version.suffix, dirtyMarker)
        }
        return formattedVersion
    }

    private fun Info.hasTag() = version.suffix == null

    private fun Info.hasVersionBuildInformation() = version.build != ""

    private fun Info.hasVersionPrerelease() = version.prerelease != ""

    private fun appendSuffix(version: String, snapshotSuffix: String): String {
        if (snapshotSuffix != "") {
            return "$version-$snapshotSuffix"
        }
        return version
    }

    private fun Info.hasFirstCommit(): Boolean {
        if (count == 0) {
            return false
        }
        return true
    }

    private fun appendDirtyMarker(version: String, suffix: Suffix?, dirtyMarker: String): String {
        if (suffix != null && suffix.dirty) {
            if (dirtyMarker != "") {
                return "$version-$dirtyMarker"
            }
            return version
        }
        return version
    }
}
