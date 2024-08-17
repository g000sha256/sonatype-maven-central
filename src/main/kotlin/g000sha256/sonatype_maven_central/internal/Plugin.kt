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

import g000sha256.sonatype_maven_central.SonatypeMavenCentralType
import g000sha256.sonatype_maven_central.internal.util.createDirectory
import g000sha256.sonatype_maven_central.internal.util.uploadBundle
import g000sha256.sonatype_maven_central.internal.util.zipFromDirectory
import java.io.File
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin

internal fun Project.initPlugin(
    usernameProperty: Property<String>,
    passwordProperty: Property<String>,
    typeProperty: Property<SonatypeMavenCentralType>
) {
    plugins.apply(MavenPublishPlugin::class.java)
    plugins.apply(SigningPlugin::class.java)

    val buildDirectory = layout.buildDirectory.asFile.get()
    val pluginDirectory = createDirectory(buildDirectory, "sonatype_maven_central")
    val bundleDirectory = createDirectory(pluginDirectory, "bundle")
    val repositoryDirectory = createDirectory(pluginDirectory, "repository")

    initShutdownHook { clearDirectories(bundleDirectory, repositoryDirectory) }

    val publishingExtension = extensions.getByType(PublishingExtension::class.java)

    publishingExtension.repositories.maven { it.init(repositoryDirectory) }

    publishingExtension.publications.withType(MavenPublication::class.java) { mavenPublication ->
        val variant = mavenPublication.name.replaceFirstChar(Char::uppercase)

        tasks.named("publish${variant}PublicationToSonatypeMavenCentralRepository") {
            it.doLast {
                val username = usernameProperty.getTrimmedValue()
                val password = passwordProperty.getTrimmedValue()

                requireNotNull(username) { "Username is null or blank" }
                requireNotNull(password) { "Password is null or blank" }

                val type = typeProperty.getStringType()

                val versionDirectory = mavenPublication.getVersionDirectory(repositoryDirectory)
                val deploymentName = mavenPublication.getDeploymentName()
                val bundleFile = File(bundleDirectory, "$deploymentName.zip")

                bundleFile.delete()
                zipFromDirectory(repositoryDirectory, versionDirectory, bundleFile)

                uploadBundle(username, password, deploymentName, type, bundleFile)
            }
        }
    }
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

private fun MavenPublication.getVersionDirectory(repositoryDirectory: File): File {
    val paths = groupId.split(".") + artifactId + version
    return paths.fold(repositoryDirectory) { file, path -> File(file, path) }
}

private fun MavenPublication.getDeploymentName(): String {
    return "$groupId:$artifactId:$version"
}