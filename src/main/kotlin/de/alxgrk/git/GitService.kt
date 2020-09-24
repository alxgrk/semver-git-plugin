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
package de.alxgrk.git

import de.alxgrk.RunnerResult.Failure
import de.alxgrk.RunnerResult.Success
import org.apache.maven.project.MavenProject

object GitService {
    fun currentBranch(project: MavenProject) =
        getAllBranches(project).let { result ->
            if (result is Success)
                Success(filterCurrentBranch(result.processOutput))
            else
                result
        }

    fun currentCommit(project: MavenProject) =
        GitCommandRunner.execute(project.basedir, arrayOf("rev-parse", "HEAD"))

    fun currentShortCommit(project: MavenProject) =
        GitCommandRunner.execute(project.basedir, arrayOf("rev-parse", "--short", "HEAD"))

    fun currentTag(project: MavenProject, tagPrefix: String = "") =
        GitCommandRunner.execute(
            project.basedir,
            arrayOf("describe", "--exact-match", "--match", "$tagPrefix*")
        )

    fun lastTag(project: MavenProject, tagPrefix: String = "") =
        GitCommandRunner.execute(
            project.basedir,
            arrayOf("describe", "--dirty", "--abbrev=7", "--match", "$tagPrefix*")
        )

    fun isDirty(project: MavenProject) =
        when (val result = GitCommandRunner.execute(project.basedir, arrayOf("diff", "--stat"))) {
            is Success -> result.processOutput != ""
            is Failure -> false
        }

    fun count(project: MavenProject): Int =
        when (val result = GitCommandRunner.execute(project.basedir, arrayOf("rev-list", "--count", "HEAD"))) {
            is Success -> result.processOutput.toInt()
            is Failure -> 0
        }

    fun getCommitsSinceLastTag(project: MavenProject): List<String> =
        when (val result = GitCommandRunner.execute(
            project.basedir,
            arrayOf("log", "--online", "\$(git describe --tags --abbrev=0 @^)..@")
        )) {
            is Success -> result.processOutput.lines()
            is Failure -> emptyList()
        }

    fun getIgnoredFiles(project: MavenProject): List<String> =
        when (val result = GitCommandRunner.execute(project.basedir, arrayOf("status", "-s", "--ignored"))) {
            is Success -> result.processOutput
                .lines()
                .filter { it.startsWith("!!") }
                .map { it.substringAfter("!! ") }
            is Failure -> emptyList()
        }

    private fun filterCurrentBranch(branches: String) =
        """(\*)? +(.*?) +(.*?)?""".toRegex().find(branches)!!.groupValues[2]

    private fun getAllBranches(project: MavenProject) =
        GitCommandRunner.execute(
            project.basedir,
            arrayOf("branch", "--all", "--verbose", "--no-abbrev", "--contains")
        )
}
