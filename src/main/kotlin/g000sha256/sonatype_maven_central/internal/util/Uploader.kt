/*
 * Copyright 2024-2026 Georgii Ippolitov (g000sha256)
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

import g000sha256.sonatype_maven_central.SonatypeMavenCentralType
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Collections
import org.gradle.api.logging.Logger

private val connectTimeout = Duration.ofSeconds(10)

private val httpClient by lazy {
    return@lazy HttpClient
        .newBuilder()
        .connectTimeout(connectTimeout)
        .build()
}

internal fun uploadBundle(
    username: String,
    password: String,
    name: String,
    publishingType: SonatypeMavenCentralType,
    bundleFile: File,
    logger: Logger,
) {
    checkParameters(bundleFile)

    val token = encodeBase64("$username:$password")
    val deploymentId = upload(token, name, publishingType.type, bundleFile)

    logger.lifecycle("The bundle was successfully uploaded to Sonatype Maven Central: deploymentId=$deploymentId")
    if (publishingType == SonatypeMavenCentralType.Manual) {
        logger.lifecycle("The deployment is awaiting manual publishing at https://central.sonatype.com/publishing/deployments")
    }
}

private fun checkParameters(bundleFile: File) {
    bundleFile.ensureExists()
    bundleFile.ensureNotDirectory()
}

private fun upload(token: String, name: String, type: String, bundleFile: File): String {
    val response = postRequest(token, name, type, bundleFile)
    checkResponseStatus(response)
    return response.body()
}

private fun postRequest(token: String, name: String, type: String, bundleFile: File): HttpResponse<String> {
    val uri = buildUri(name, type)
    val boundary = System
        .nanoTime()
        .toString(radix = 16)
        .let { text -> "SonatypeMavenCentralBoundary$text" }
    val bodyPublisher = BodyPublishers.ofInputStream { buildMultipartStream(boundary, bundleFile) }
    val bodyHandler = BodyHandlers.ofString()
    return HttpRequest
        .newBuilder()
        .uri(uri)
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "multipart/form-data; boundary=$boundary")
        .POST(bodyPublisher)
        .build()
        .let { request -> httpClient.send(request, bodyHandler) }
}

private fun buildUri(name: String, type: String): URI {
    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8)
    val encodedType = URLEncoder.encode(type, StandardCharsets.UTF_8)
    return URI.create("https://central.sonatype.com/api/v1/publisher/upload?name=$encodedName&publishingType=$encodedType")
}

private fun buildMultipartStream(boundary: String, file: File): InputStream {
    val headerString = buildString {
        append("--")
        append(boundary)
        append("\r\n")
        append("Content-Disposition: form-data; name=\"bundle\"; filename=\"")
        append(file.name)
        append("\"\r\n")
        append("Content-Type: application/octet-stream\r\n")
        append("\r\n")
    }
    val header = headerString.toByteArray(StandardCharsets.UTF_8)
    val footerString = "\r\n--$boundary--\r\n"
    val footer = footerString.toByteArray(StandardCharsets.UTF_8)
    val streams = listOf(
        ByteArrayInputStream(header),
        FileInputStream(file),
        ByteArrayInputStream(footer),
    )
    val enumeration = Collections.enumeration(streams)
    return SequenceInputStream(enumeration)
}

private fun checkResponseStatus(response: HttpResponse<*>) {
    val status = response.statusCode()
    require(status == 201) { "Failed to upload bundle: HTTP $status" }
}
