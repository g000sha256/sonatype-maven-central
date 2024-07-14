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

package g000sha256.sonatype_maven_central.internal.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun zipFromDirectory(rootDirectory: File, filesDirectory: File, zipFile: File) {
    checkParameters(rootDirectory, filesDirectory, zipFile)
    createZipFromDirectory(rootDirectory, filesDirectory, zipFile)
}

private fun checkParameters(rootDirectory: File, filesDirectory: File, zipFile: File) {
    rootDirectory.ensureExists()
    rootDirectory.ensureDirectory()

    filesDirectory.ensureExists()
    filesDirectory.ensureDirectory()

    filesDirectory.ensureIsInsideIn(rootDirectory)

    zipFile.ensureNotExists()
}

private fun createZipFromDirectory(rootDirectory: File, filesDirectory: File, zipFile: File) {
    val outputStream = zipFile.createZipOutputStream()
    outputStream.use { it.writeDirectory(rootDirectory, filesDirectory) }
}

private fun File.createZipOutputStream(): ZipOutputStream {
    val outputStream = FileOutputStream(this)
    return ZipOutputStream(outputStream)
}

private fun ZipOutputStream.writeDirectory(rootDirectory: File, filesDirectory: File) {
    val files = filesDirectory.listFiles()
    files?.forEach { writeAnyFile(rootDirectory, it) }
}

private fun ZipOutputStream.writeAnyFile(rootDirectory: File, file: File) {
    if (file.isDirectory) {
        writeDirectory(rootDirectory, file)
    } else {
        writeFile(rootDirectory, file)
    }
}

private fun ZipOutputStream.writeFile(rootDirectory: File, file: File) {
    val name = file.toRelativeString(rootDirectory)
    writeFile(file, name)
}

private fun ZipOutputStream.writeFile(file: File, name: String) {
    val inputStream = FileInputStream(file)
    inputStream.use { writeInputStream(name, it) }
}

private fun ZipOutputStream.writeInputStream(name: String, inputStream: InputStream) {
    openEntry(name)
    inputStream.transferTo(this)
    closeEntry()
}

private fun ZipOutputStream.openEntry(name: String) {
    val entry = ZipEntry(name)
    putNextEntry(entry)
}