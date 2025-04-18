/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.opentelemetry.android.auth.kotlin

import aws.smithy.kotlin.runtime.InternalApi
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.auth.awssigning.AwsSignedBodyHeader
import aws.smithy.kotlin.runtime.auth.awssigning.AwsSigningConfig
import aws.smithy.kotlin.runtime.auth.awssigning.DefaultAwsSigner
import aws.smithy.kotlin.runtime.http.HttpMethod
import aws.smithy.kotlin.runtime.http.request.HttpRequestBuilder
import aws.smithy.kotlin.runtime.http.toHttpBody
import aws.smithy.kotlin.runtime.net.url.Url

/**
 * This is a simple utility object that generates SigV4 signed headers given some auth inputs
 */
object AwsRequestSigner {
    private const val CONTENT_TYPE = "application/x-protobuf"

    @OptIn(InternalApi::class)
    suspend fun getSignedHeaders(
        input: ByteArray,
        credentialsProvider: CredentialsProvider,
        serviceName: String,
        endpoint: String,
        region: String,
    ): Map<String, String> {
        val credentials = credentialsProvider.resolve()

        val awsSigningConfig =
            AwsSigningConfig
                .Builder()
                .apply {
                    this.region = region
                    this.service = serviceName
                    this.credentials = credentials
                    this.signedBodyHeader = AwsSignedBodyHeader.X_AMZ_CONTENT_SHA256
                }.build()

        val url = Url.parse(endpoint)

        val httpRequest =
            HttpRequestBuilder()
                .apply {
                    this.method = HttpMethod.POST
                    this.url.copyFrom(url)
                    this.headers.append("Content-Type", CONTENT_TYPE)
                    this.body = input.toHttpBody()
                }.build()

        val signedRequest = DefaultAwsSigner.sign(httpRequest, awsSigningConfig)

        return signedRequest
            .output
            .headers
            .entries()
            .associate { it.key to it.value[0] }
    }
}
