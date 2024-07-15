/*
 * Copyright 2024 Georgii Ippolitov (g000sha256)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package g000sha256.sonatype_maven_central.internal

import g000sha256.sonatype_maven_central.SonatypeMavenCentralCredentials
import g000sha256.sonatype_maven_central.SonatypeMavenCentralRepository
import g000sha256.sonatype_maven_central.SonatypeMavenCentralType
import g000sha256.sonatype_maven_central.internal.task.CreateZipTask
import g000sha256.sonatype_maven_central.internal.task.UploadTask
import g000sha256.sonatype_maven_central.internal.util.createDirectory
import java.io.File
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

internal fun Project.plugin(block: SonatypeMavenCentralRepository.() -> Unit) {
    val inputData = collectInputData(block)

    val buildDirectory = layout.buildDirectory.asFile.get()
    val pluginDirectory = createDirectory(buildDirectory, "sonatype_maven_central")
    val bundleDirectory = createDirectory(pluginDirectory, "bundle")
    val repositoryDirectory = createDirectory(pluginDirectory, "repository")

    initShutdownHook { clearDirectories(bundleDirectory, repositoryDirectory) }

    afterEvaluate {
        val publishingExtension = extensions.getByType<PublishingExtension>()

        publishingExtension.repositories.maven { it.init(repositoryDirectory) }

        val mavenPublications = publishingExtension.getMavenPublications()
        mavenPublications.forEach { mavenPublication ->
            val variant = mavenPublication.name.capitalized()

            val versionDirectory = mavenPublication.getVersionDirectory(repositoryDirectory)
            val deploymentName = mavenPublication.getDeploymentName()
            val bundleFile = File(bundleDirectory, "$deploymentName.zip")

            val createZipTaskData = CreateZipTask.Data(repositoryDirectory, versionDirectory, bundleFile)
            val uploadTaskData = UploadTask.Data(inputData.username, inputData.password, deploymentName, inputData.type, bundleFile)

            val publishTask = tasks.named("publish${variant}PublicationToSonatypeMavenCentralRepository")
            val createZipTask = tasks.register<CreateZipTask>("createZip$variant", createZipTaskData)
            val uploadTask = tasks.register<UploadTask>("upload$variant", uploadTaskData)

            publishTask.configure { it.finalizedBy(createZipTask) }

            val group = "sonatype maven central"
            createZipTask.configure {
                it.group = group
                it.dependsOn(publishTask)
                it.mustRunAfter(publishTask)
                it.finalizedBy(uploadTask)
            }

            uploadTask.configure {
                it.group = group
                it.dependsOn(createZipTask)
                it.mustRunAfter(createZipTask)
            }
        }
    }
}

private fun Project.collectInputData(block: SonatypeMavenCentralRepository.() -> Unit): InputData {
    val credentials = objects.newInstance<SonatypeMavenCentralCredentials>()
    val repository = objects.newInstance<RepositoryWrapper>(credentials)
    repository.block()

    val username = credentials.username.getTrimmedValue()
    val password = credentials.password.getTrimmedValue()

    requireNotNull(username) { "Username is null or blank" }
    requireNotNull(password) { "Password is null or blank" }

    val type = repository.type.getStringType()
    return InputData(username, password, type)
}

private fun Property<String>.getTrimmedValue(): String? {
    val rawValue = getOrNull()
    if (rawValue == null) {
        return null
    }

    val trimmedValue = rawValue.trim()
    if (trimmedValue.length == 0) {
        return null
    }

    return trimmedValue
}

private fun Property<SonatypeMavenCentralType>.getStringType(): String {
    val type = getOrElse(SonatypeMavenCentralType.Manual)
    when (type) {
        SonatypeMavenCentralType.Automatic -> return "AUTOMATIC"
        SonatypeMavenCentralType.Manual -> return "USER_MANAGED"
    }
}

private fun clearDirectories(bundleDirectory: File, repositoryDirectory: File) {
    bundleDirectory.clearDirectory()
    repositoryDirectory.clearDirectory()
}

private fun File.clearDirectory() {
    val files = listFiles()
    files?.forEach { it.deleteRecursively() }
}

private fun initShutdownHook(runnable: Runnable) {
    val thread = Thread(runnable, "ShutdownHook")
    initShutdownHook(thread)
}

private fun initShutdownHook(thread: Thread) {
    val runtime = Runtime.getRuntime()
    runtime.addShutdownHook(thread)
}

private fun MavenArtifactRepository.init(directory: File) {
    name = "SonatypeMavenCentral"
    setUrl(directory)
}

private fun PublishingExtension.getMavenPublications(): List<MavenPublication> {
    val mavenPublications = publications.withType<MavenPublication>()
    return mavenPublications.toList()
}

private fun MavenPublication.getVersionDirectory(repositoryDirectory: File): File {
    val paths = groupId.split(".") + artifactId + version
    return paths.fold(repositoryDirectory) { file, path -> File(file, path) }
}

private fun MavenPublication.getDeploymentName(): String {
    return "$groupId:$artifactId:$version"
}

private abstract class RepositoryWrapper @Inject constructor(
    private val credentials: SonatypeMavenCentralCredentials
) : SonatypeMavenCentralRepository {

    override fun credentials(block: SonatypeMavenCentralCredentials.() -> Unit) {
        credentials.block()
    }

}

private class InputData(val username: String, val password: String, val type: String)