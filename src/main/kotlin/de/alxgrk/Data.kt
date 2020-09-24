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
import de.alxgrk.incrementer.Incrementer
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

class Branch {

    @Parameter
    var regex: String = ".+"

    @Parameter
    var incrementer: String = "NO_VERSION_INCREMENTER"

    @Parameter
    var formatter: String = DEFAULT_FORMATTER

    /**
     * Valid templates are:
     *  - $MAJOR
     *  - $MINOR
     *  - $PATCH
     *  - $COUNT
     *  - $SHORT_COMMIT
     */
    val formatterFunction = formatter.toFormatterFunction()

    companion object {
        const val DEFAULT_FORMATTER = "\$MAJOR.\$MINOR.\$PATCH+build.\$COUNT.sha.\$SHORT_COMMIT"

        val DEFAULT_FORMATTER_FUNCTION = DEFAULT_FORMATTER.toFormatterFunction()

        fun String.toFormatterFunction(): (Info) -> String = { info: Info ->
                this.replace("\$MAJOR", info.version.major.toString())
                    .replace("\$MINOR", info.version.minor.toString())
                    .replace("\$PATCH", info.version.patch.toString())
                    .replace("\$COUNT", info.count.toString())
                    .replace("\$SHORT_COMMIT", info.shortCommit)
            }
    }
}

data class Version(
    var major: Int,
    var minor: Int,
    var patch: Int,
    var prerelease: String,
    var build: String,
    var suffix: Suffix?
)

data class Suffix(var count: Int, var sha: String, var dirty: Boolean)

data class CurrentBranch(private var project: MavenProject) {
    val group: String
        get() = this.name.split("/")[0]

    val name: String
        get() =
            when (val result = GitService.currentBranch(project)) {
                is Success -> result.processOutput
                is Failure -> ""
            }

    val id: String
        get() = this.name.replace("/", "-")
}

sealed class RunnerResult {

    class Success(val processOutput: String) : RunnerResult() {
        override fun toString() = processOutput
    }

    class Failure(val errorMessage: String) : RunnerResult()
}

sealed class VersionResult {

    class Found(val version: Version) : VersionResult()

    object NoCurrentVersionFound : VersionResult()

    object NoIncrementerFound : VersionResult()

    object NoLastVersionFound : VersionResult()

    object NoCurrentCommitFound : VersionResult()
}

sealed class IncrementerResult {

    class Found(val incrementer: Incrementer) : IncrementerResult()

    object NoIncrementerFound : IncrementerResult()
}