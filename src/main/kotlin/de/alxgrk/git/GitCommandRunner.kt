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

import de.alxgrk.RunnerResult
import de.alxgrk.RunnerResult.Failure
import de.alxgrk.RunnerResult.Success
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

object GitCommandRunner {
    fun execute(projectDir: File, args: Array<String>): RunnerResult {
        val process = startGitProcess(args, projectDir)
        waitForGitProcess(process)

        return if (processFinishedWithoutErrors(process) && process.inputStream != null)
            Success(readProcessOutput(process.inputStream!!))
        else
            Failure("Executing git command failed with " + process.exitValue())
    }

    private fun readProcessOutput(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }.trim()
    }

    private fun processFinishedWithoutErrors(process: Process): Boolean {
        if (process.exitValue() == 0) {
            return true
        }
        return false
    }

    private fun waitForGitProcess(process: Process) {
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroy()
            throw RuntimeException("Execution timed out: $this")
        }
    }

    private fun startGitProcess(args: Array<String>, projectDir: File): Process {
        return ProcessBuilder("git", *args)
            .directory(projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
    }
}
