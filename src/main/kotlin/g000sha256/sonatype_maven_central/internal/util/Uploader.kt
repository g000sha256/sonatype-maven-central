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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.content.PartData
import io.ktor.util.cio.readChannel
import java.io.File
import kotlinx.coroutines.runBlocking

private val httpClient by lazy { HttpClient(Java) }

internal fun uploadBundle(username: String, password: String, name: String, type: String, bundleFile: File) {
    checkParameters(bundleFile)

    val token = encodeBase64("$username:$password")
    val deploymentId = runBlocking { upload(token, name, type, bundleFile) }

    println("The bundle was successfully uploaded to Sonatype Maven Central: deploymentId=$deploymentId")
}

private fun checkParameters(bundleFile: File) {
    bundleFile.ensureExists()
    bundleFile.ensureNotDirectory()
}

private suspend fun upload(token: String, name: String, type: String, bundleFile: File): String {
    val response = postRequest(token, name, type, bundleFile)
    checkResponseStatus(response)
    return response.body()
}

private suspend fun postRequest(token: String, name: String, type: String, bundleFile: File): HttpResponse {
    return httpClient.post {
        url {
            protocol = URLProtocol.HTTPS
            host = "central.sonatype.com"

            appendPathSegments("api", "v1", "publisher", "upload")

            parameters.append("name", name)
            parameters.append("publishingType", type)
        }

        header(HttpHeaders.Authorization, "Bearer $token")

        body("bundle", bundleFile)
    }
}

private fun HttpRequestBuilder.body(key: String, file: File) {
    val formData = formData { appendFile(key, file) }
    multiPartFormDataBody(formData)
}

private fun FormBuilder.appendFile(key: String, file: File) {
    val channelProvider = createFormPartChannelProvider(file)
    val headers = Headers.build { appendFormPartHeaders(file.name) }
    append(key, channelProvider, headers)
}

private fun createFormPartChannelProvider(file: File): ChannelProvider {
    val size = file.length()
    return ChannelProvider(size) { file.readChannel() }
}

private fun HeadersBuilder.appendFormPartHeaders(fileName: String) {
    append(HttpHeaders.ContentDisposition) { appendFormPartHeadersFileName(fileName) }
    append(HttpHeaders.ContentType) { append(ContentType.Application.OctetStream) }
}

private fun StringBuilder.appendFormPartHeadersFileName(fileName: String) {
    append(ContentDisposition.Parameters.FileName)
    append("=")
    append("\"")
    append(fileName)
    append("\"")
}

private fun HeadersBuilder.append(key: String, builder: StringBuilder.() -> Unit) {
    val value = buildString(builder)
    append(key, value)
}

private fun HttpRequestBuilder.multiPartFormDataBody(formData: List<PartData>) {
    val body = MultiPartFormDataContent(formData)
    setBody(body)
}

private fun checkResponseStatus(response: HttpResponse) {
    val status = response.status
    require(status == HttpStatusCode.Created) { "code=${status.value}, description=${status.description}" }
}