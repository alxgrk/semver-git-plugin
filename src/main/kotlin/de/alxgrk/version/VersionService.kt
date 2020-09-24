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

import de.alxgrk.*
import de.alxgrk.git.GitService
import de.alxgrk.incrementer.Incrementers
import de.alxgrk.incrementer.MinorVersionIncrementer
import org.apache.maven.project.MavenProject

class VersionService(
    private val project: MavenProject,
    private val tagPrefix: String,
    private val initialVersion: String,
    private val branches: List<Branch>,
    private val currentBranch: CurrentBranch
) {
    private val versionFactory: VersionFactory = SemanticVersionFactory

    fun getVersion(): Version =
        when (val result = getCurrentVersion()) {
            is VersionResult.Found -> result.version
            is VersionResult.NoCurrentVersionFound -> handleNoCurrentTagFound(project)
            else -> throw RuntimeException(result::class.simpleName)
        }

    private fun handleNoCurrentTagFound(project: MavenProject): Version =
        when (val result = incrementVersion(getLastVersion(), project)) {
            is VersionResult.Found -> result.version
            is VersionResult.NoLastVersionFound -> buildInitialVersion()
            else -> throw RuntimeException(result::class.simpleName)
        }

    private fun buildInitialVersion(): Version =
        when (val result = buildInitialVersionForTag()) {
            is VersionResult.Found -> result.version
            is VersionResult.NoCurrentCommitFound -> buildInitialVersionWithNoTag()
            else -> throw RuntimeException(result::class.simpleName)
        }

    private fun buildInitialVersionWithNoTag() = Version(0, 1, 0, "", "", null)

    private fun buildInitialVersionForTag(): VersionResult =
        when (val sha = GitService.currentShortCommit(project)) {
            is RunnerResult.Success -> {
                val isDirty = GitService.isDirty(project)
                val count = GitService.count(project)
                val version = versionFactory.createFromString(initialVersion)
                version.suffix = Suffix(count, sha.processOutput, isDirty)
                VersionResult.Found(version)
            }
            is RunnerResult.Failure -> VersionResult.NoCurrentCommitFound
        }

    private fun getLastVersion() =
        when (val lastTag = GitService.lastTag(project, tagPrefix)) {
            is RunnerResult.Success -> {
                if (!lastTag.processOutput.startsWith(tagPrefix)) {
                    VersionResult.NoLastVersionFound
                } else {
                    val tagWithoutPrefix = lastTag.processOutput.substring(tagPrefix.length)
                    VersionResult.Found(versionFactory.createFromString(tagWithoutPrefix))
                }
            }
            is RunnerResult.Failure -> VersionResult.NoLastVersionFound
        }

    private fun getCurrentVersion() =
        when (val curTag = GitService.currentTag(project, tagPrefix)) {
            is RunnerResult.Success ->
                if (curTag.processOutput.startsWith(tagPrefix))
                    VersionResult.Found(versionFactory.createFromString(curTag.processOutput.substring(tagPrefix.length)))
                else
                    VersionResult.NoCurrentVersionFound
            is RunnerResult.Failure -> VersionResult.NoCurrentVersionFound
        }

    private fun incrementVersion(versionResult: VersionResult, project: MavenProject): VersionResult {
        if (versionResult !is VersionResult.Found)
            return versionResult

        val version = versionResult.version
        val regexIncrementerPair = RegexResolver.findMatchingRegex(
            branches,
            currentBranch.name
        )
        return regexIncrementerPair
            ?.let {
                when (val result = Incrementers.getVersionIncrementerByName(regexIncrementerPair.incrementer)) {
                    is IncrementerResult.Found -> VersionResult.Found(
                        result.incrementer.increment(
                            version,
                            project
                        )
                    )
                    is IncrementerResult.NoIncrementerFound -> VersionResult.NoIncrementerFound
                }
            }
            ?: run {
                VersionResult.Found(MinorVersionIncrementer().increment(version, project))
            }
    }
}
