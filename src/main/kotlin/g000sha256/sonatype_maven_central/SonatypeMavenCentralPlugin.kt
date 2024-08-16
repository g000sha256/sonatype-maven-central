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

package g000sha256.sonatype_maven_central

import g000sha256.sonatype_maven_central.internal.initPlugin
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

@Deprecated("Use sonatypeMavenCentralRepository directly without importing the extension.")
public fun Project.sonatypeMavenCentralRepository(block: SonatypeMavenCentralRepository.() -> Unit) {
    val credentials = objects.newInstance(SonatypeMavenCentralCredentials::class.java)
    val repository = objects.newInstance(SonatypeMavenCentralPlugin.RepositoryWrapper::class.java, credentials)

    repository.block()

    initPlugin(credentials.username, credentials.password, repository.type)
}

public class SonatypeMavenCentralPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val credentials = target.objects.newInstance(SonatypeMavenCentralCredentials::class.java)
        val repository = target.objects.newInstance(RepositoryWrapper::class.java, credentials)

        target.extensions.add(SonatypeMavenCentralRepository::class.java, "sonatypeMavenCentralRepository", repository)

        target.initPlugin(credentials.username, credentials.password, repository.type)
    }

    internal abstract class RepositoryWrapper @Inject constructor(
        private val credentials: SonatypeMavenCentralCredentials
    ) : SonatypeMavenCentralRepository {

        override fun credentials(block: SonatypeMavenCentralCredentials.() -> Unit) {
            credentials.block()
        }

    }

}