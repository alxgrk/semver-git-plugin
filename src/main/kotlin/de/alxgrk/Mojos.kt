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
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.LifecyclePhase.VALIDATE
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.lang.management.ManagementFactory
import kotlin.system.exitProcess

const val DIRTY_MARKER_DEFAULT = "dirty"
const val INITIAL_VERSION_DEFAULT = "0.1.0"
const val SNAPSHOT_SUFFIX_DEFAULT = "SNAPSHOT"
const val TAG_PREFIX_DEFAULT = ""

@Mojo(name = "setVersion", defaultPhase = VALIDATE)
class SetVersionMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter(required = true)
    lateinit var branches: List<Branch>

    @Parameter(defaultValue = DIRTY_MARKER_DEFAULT)
    lateinit var dirtyMarker: String

    @Parameter(defaultValue = INITIAL_VERSION_DEFAULT)
    lateinit var initialVersion: String

    @Parameter(defaultValue = SNAPSHOT_SUFFIX_DEFAULT)
    lateinit var snapshotSuffix: String

    @Parameter(defaultValue = TAG_PREFIX_DEFAULT)
    lateinit var tagPrefix: String

    override fun execute() {
        val ignoredFiles = GitService.getIgnoredFiles(project)
        if (ignoredFiles.none { it.endsWith(".mvn/") || it.endsWith(".mvn/maven.config") }) {
            log.warn(
                "The file '.mvn/maven.config' is not git-ignored - please add it to .gitignore," +
                        "otherwise version changes would need to be checked in into version control."
            )
        }

        val mavenConfigFile = project.basedir.toPath().resolve(".mvn").resolve("maven.config").toFile()
        if (!mavenConfigFile.exists()) {
            log.info("Writing initial version '$initialVersion' to '.mvn/maven.config'.")
            mavenConfigFile.writeText("-Drevision=$initialVersion")
            log.error("The file '.mvn/maven.config' didn't exist before, so the correct version couldn't be set - retry your command now.")
            exitProcess(1)
        }

        val info = Info(project, branches, tagPrefix, initialVersion, snapshotSuffix, dirtyMarker)

        val currentVersion = mavenConfigFile.readLines()
            .filter { it.startsWith("-Drevision") }
            .map { it.split("=")[1] }
            .firstOrNull()
            ?: log.error("Property '-Drevision' was not set in '.mvn/maven.config'.").let {
                exitProcess(1)
            }

        if (currentVersion != info.toString()) {
            mavenConfigFile.writeText("-Drevision=$info")
            log.info("Set version to $info")
            // TODO restart
        } else {
            log.info("Current version is already up to date.")
        }
    }
}

@Mojo(name = "test")
class TestMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter(defaultValue = "\${session}", readonly = true)
    lateinit var session: MavenSession

    @Parameter(defaultValue = "\${mojoExecution}", readonly = true)
    lateinit var mojo: MojoExecution

    @Parameter(defaultValue = "\${plugin}", readonly = true)
    lateinit var plugin: PluginDescriptor

    override fun execute() {

        val java = System.getProperty("java.home") + "/bin/java"
        val vmArguments = ManagementFactory.getRuntimeMXBean().inputArguments
            .filter {
                // if it's the agent argument : we ignore it otherwise the
                // address of the old application and the new one will be in conflict
                !it.contains("-agentlib")
            }.toTypedArray()

        val cmd = mutableListOf<String>(java, *vmArguments)

        // program main and program arguments (be careful a sun property. might not be supported by all JVM)
        val mainCommand: Array<String> = System.getProperty("sun.java.command").split(" ").toTypedArray()

        if (mainCommand[0].endsWith(".jar")) {
            cmd.add("-jar")
            cmd.add(File(mainCommand[0]).path.toString())
        } else {
            cmd.add("-cp")
            cmd.add("\"${System.getProperty("java.class.path")}\"")
            cmd.add(mainCommand[0])
        }

        for (i in 1 until mainCommand.size) {
            cmd.add(mainCommand[i])
        }

        // execute the command in a shutdown hook, to be sure that all the
        // resources have been disposed before restarting the application
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                val process = ProcessBuilder(cmd)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()
                log.info(
                    "Subprocess terminated with result '${
                        process?.inputStream?.bufferedReader()?.use { it.readText() }?.trim() ?: "error"
                    }'"
                )
                log.error(
                    "Subprocess terminated weh errors '${
                        process?.errorStream?.bufferedReader()?.use { it.readText() }?.trim() ?: "error"
                    }'"
                )
            }
        })

        log.info("Going to restart using command '${cmd.joinToString(" ")}'")

        exitProcess(0)
    }
}

@Mojo(name = "showVersion")
class ShowVersionMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter(required = true)
    lateinit var branches: List<Branch>

    @Parameter(defaultValue = DIRTY_MARKER_DEFAULT)
    lateinit var dirtyMarker: String

    @Parameter(defaultValue = INITIAL_VERSION_DEFAULT)
    lateinit var initialVersion: String

    @Parameter(defaultValue = SNAPSHOT_SUFFIX_DEFAULT)
    lateinit var snapshotSuffix: String

    @Parameter(defaultValue = TAG_PREFIX_DEFAULT)
    lateinit var tagPrefix: String
    override fun execute() {
        log.info("Version: ${Info(project, branches, tagPrefix, initialVersion, snapshotSuffix, dirtyMarker)}")
    }
}

@Mojo(name = "showInfo")
class ShowInfoMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    lateinit var project: MavenProject

    @Parameter(required = true)
    lateinit var branches: List<Branch>

    @Parameter(defaultValue = DIRTY_MARKER_DEFAULT)
    lateinit var dirtyMarker: String

    @Parameter(defaultValue = INITIAL_VERSION_DEFAULT)
    lateinit var initialVersion: String

    @Parameter(defaultValue = SNAPSHOT_SUFFIX_DEFAULT)
    lateinit var snapshotSuffix: String

    @Parameter(defaultValue = TAG_PREFIX_DEFAULT)
    lateinit var tagPrefix: String

    override fun execute() {
        val info = Info(project, branches, tagPrefix, initialVersion, snapshotSuffix, dirtyMarker)

        log.info("Branch name: ${info.currentBranch.name}")
        log.info("Branch group: ${info.currentBranch.group}")
        log.info("Branch id: ${info.currentBranch.id}")
        log.info("Commit: ${info.commit}")
        log.info("Short commit: ${info.shortCommit}")
        log.info("Tag: ${info.tag}")
        log.info("Last tag: ${info.lastTag}")
        log.info("Dirty: ${info.dirty}")
        log.info("Version: $info")
        log.info("Version major: ${info.version.major}")
        log.info("Version minor: ${info.version.minor}")
        log.info("Version patch: ${info.version.patch}")
        log.info("Version pre release: ${if (info.version.prerelease.isEmpty()) "none" else info.version.prerelease}")
        log.info("Version build: ${if (info.version.build.isEmpty()) "none" else info.version.build}")
    }
}
