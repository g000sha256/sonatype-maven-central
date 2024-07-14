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

package g000sha256.sonatype_maven_central.internal.task

import g000sha256.sonatype_maven_central.internal.util.uploadBundle
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

internal abstract class UploadTask @Inject constructor(private val data: Data) : DefaultTask() {

    @TaskAction
    fun execute() {
        uploadBundle(data.username, data.password, data.name, data.type, data.bundleFile)
    }

    class Data(val username: String, val password: String, val name: String, val type: String, val bundleFile: File)

}