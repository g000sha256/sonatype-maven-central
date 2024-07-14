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

internal fun createDirectory(rootDirectory: File, path: String): File {
    val directory = File(rootDirectory, path)
    directory.mkdirs()
    return directory
}

internal fun File.ensureExists() {
    val exists = exists()
    require(exists) { "The file $this doesn't exist" }
}

internal fun File.ensureNotExists() {
    val exists = exists()
    require(!exists) { "The file $this already exists" }
}

internal fun File.ensureDirectory() {
    require(isDirectory) { "The file $this isn't a directory" }
}

internal fun File.ensureNotDirectory() {
    require(!isDirectory) { "The file $this is a directory" }
}

internal fun File.ensureIsInsideIn(rootDirectory: File) {
    val isInside = startsWith(rootDirectory)
    require(isInside) { "The file $this isn't inside the directory $rootDirectory" }
}