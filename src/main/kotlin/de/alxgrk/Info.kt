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

import de.alxgrk.formatter.SemanticVersionFormatter.format
import de.alxgrk.git.GitService
import de.alxgrk.version.VersionService
import org.apache.maven.project.MavenProject

data class Info(
    private val project: MavenProject,
    private val branches: List<Branch>,
    private val tagPrefix: String,
    private val initialVersion: String,
    private val snapshotSuffix: String,
    private val dirtyMarker: String
) {
    val currentBranch: CurrentBranch
        get() = CurrentBranch(project)

    val commit: String
        get() = outputOrNone { GitService.currentCommit(project) }

    val shortCommit: String
        get() = outputOrNone { GitService.currentShortCommit(project) }

    val tag: String
        get() = outputOrNone { GitService.currentTag(project) }

    val lastTag: String
        get() = outputOrNone { GitService.lastTag(project) }

    val dirty: Boolean
        get() = GitService.isDirty(project)

    val count: Int
        get() = GitService.count(project)

    val version: Version
        get() = VersionService(project, tagPrefix, initialVersion, branches, currentBranch).getVersion()

    private fun outputOrNone(block: () -> RunnerResult): String {
        return when (val result = block()) {
            is RunnerResult.Success -> result.processOutput
            is RunnerResult.Failure -> "none"
        }
    }

    override fun toString(): String {
        return format(
            branches,
            snapshotSuffix,
            dirtyMarker
        )
    }
}
