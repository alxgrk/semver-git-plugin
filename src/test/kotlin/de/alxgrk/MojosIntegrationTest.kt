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

import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
class MojosIntegrationTest : AbstractMojoTestCase() {

    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `showing version works`() {
        val testPom = File(getBasedir(), "src/test/resources/test-pom.xml")

        val mojo = lookupMojo("showVersion", testPom) as ShowVersionMojo

        assertNotNull(mojo)
    }

    fun initializeGitWithBranch(directory: File, tag: String = "0.1.0", branch: String = "develop"): Git {
        val git = Git.init().setDirectory(directory).call()
        val commit = git.commit().setMessage("").call()
        git.checkout().setCreateBranch(true).setName(branch).call()
        git.tag().setName(tag).setObjectId(commit).call()
        return git
    }

    fun initializeGitWithoutBranch(directory: File, tag: String = "0.1.0"): Git {
        val git = Git.init().setDirectory(directory).call()
        val commit = git.commit().setMessage("").call()
        git.tag().setName(tag).setObjectId(commit).call()
        return git
    }
    fun initializeGitWithoutBranchAndWithoutTag(directory: File): Git {
        val git = Git.init().setDirectory(directory).call()
        val commit = git.commit().setMessage("").call()
        return git
    }
}
