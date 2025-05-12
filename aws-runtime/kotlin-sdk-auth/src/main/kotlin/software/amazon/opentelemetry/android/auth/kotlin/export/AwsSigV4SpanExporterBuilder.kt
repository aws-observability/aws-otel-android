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
package software.amazon.opentelemetry.android.auth.kotlin.export

import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import io.opentelemetry.sdk.trace.export.SpanExporter

class AwsSigV4SpanExporterBuilder {
    private lateinit var endpoint: String
    private lateinit var region: String
    private lateinit var serviceName: String
    private lateinit var credentialsProvider: CredentialsProvider
    private var parentExporter: SpanExporter? = null

    fun setEndpoint(endpoint: String): AwsSigV4SpanExporterBuilder {
        this.endpoint = endpoint
        return this
    }

    fun setRegion(region: String): AwsSigV4SpanExporterBuilder {
        this.region = region
        return this
    }

    fun setCredentialsProvider(credentialsProvider: CredentialsProvider): AwsSigV4SpanExporterBuilder {
        this.credentialsProvider = credentialsProvider
        return this
    }

    fun setServiceName(serviceName: String): AwsSigV4SpanExporterBuilder {
        this.serviceName = serviceName
        return this
    }

    fun setParentExporter(parentExporter: SpanExporter): AwsSigV4SpanExporterBuilder {
        this.parentExporter = parentExporter
        return this
    }

    fun build(): AwsSigV4SpanExporter = AwsSigV4SpanExporter(endpoint, region, serviceName, credentialsProvider, parentExporter)
}
