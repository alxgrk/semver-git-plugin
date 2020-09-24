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
package de.alxgrk.incrementer

import de.alxgrk.git.GitService
import de.alxgrk.IncrementerResult
import de.alxgrk.Version
import org.apache.maven.project.MavenProject

interface Incrementer {

    fun increment(version: Version, project: MavenProject): Version
}

class MajorVersionIncrementer : Incrementer {
    override fun increment(version: Version, project: MavenProject): Version {
        version.major += 1
        version.minor = 0
        version.patch = 0
        return version
    }
}

class MinorVersionIncrementer : Incrementer {
    override fun increment(version: Version, project: MavenProject): Version {
        version.minor += 1
        version.patch = 0
        return version
    }
}

class PatchVersionIncrementer : Incrementer {
    override fun increment(version: Version, project: MavenProject): Version {
        version.patch += 1
        return version
    }
}

class NoVersionIncrementer : Incrementer {
    override fun increment(version: Version, project: MavenProject): Version {
        return version
    }
}

class ConventionalCommitsIncrementer : Incrementer {
    override fun increment(version: Version, project: MavenProject): Version {
        val listOfCommits = GitService.getCommitsSinceLastTag(project)
        var major = 0
        var minor = 0
        var patch = 0
        listOfCommits.forEach {
            if (it.contains("""^[0-9a-f]{7} BREAKING CHANGE""".toRegex())) {
                major += 1
            }
            if (it.contains("""^[0-9a-f]{7} feat""".toRegex())) {
                minor += 1
            }
            if (it.contains("""^[0-9a-f]{7} fix""".toRegex())) {
                patch += 1
            }
        }
        if (patch > 0) {
            version.patch += 1
        }
        if (minor > 0) {
            version.patch = 0
            version.minor += 1
        }
        if (major > 0) {
            version.patch = 0
            version.minor = 0
            version.major += 1
        }
        return version
    }
}

enum class Incrementers(private val incrementer: Incrementer) {

    NO_VERSION_INCREMENTER(NoVersionIncrementer()),
    PATCH_INCREMENTER(PatchVersionIncrementer()),
    MINOR_INCREMENTER(MinorVersionIncrementer()),
    MAJOR_INCREMENTER(MajorVersionIncrementer()),
    CONVENTIONAL_COMMITS_INCREMENTER(ConventionalCommitsIncrementer());

    companion object {
        fun getVersionIncrementerByName(name: String) =
            try {
                IncrementerResult.Found(valueOf(name.toUpperCase()).incrementer)
            } catch (ex: IllegalArgumentException) {
                IncrementerResult.NoIncrementerFound
            }
    }
}