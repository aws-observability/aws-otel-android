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
import io.opentelemetry.sdk.logs.export.LogRecordExporter

class AwsSigV4LogRecordExporterBuilder {
    private lateinit var endpoint: String
    private lateinit var region: String
    private lateinit var serviceName: String
    private lateinit var credentialsProvider: CredentialsProvider
    private lateinit var compression: String
    private var parentExporter: LogRecordExporter? = null

    fun setEndpoint(endpoint: String): AwsSigV4LogRecordExporterBuilder {
        this.endpoint = endpoint
        return this
    }

    fun setRegion(region: String): AwsSigV4LogRecordExporterBuilder {
        this.region = region
        return this
    }

    fun setCredentialsProvider(credentialsProvider: CredentialsProvider): AwsSigV4LogRecordExporterBuilder {
        this.credentialsProvider = credentialsProvider
        return this
    }

    fun setServiceName(serviceName: String): AwsSigV4LogRecordExporterBuilder {
        this.serviceName = serviceName
        return this
    }

    fun setParentExporter(parentExporter: LogRecordExporter): AwsSigV4LogRecordExporterBuilder {
        this.parentExporter = parentExporter
        return this
    }

    fun setCompression(compressionMethod: String): AwsSigV4LogRecordExporterBuilder {
        this.compression = compressionMethod
        return this
    }

    fun build(): AwsSigV4LogRecordExporter =
        AwsSigV4LogRecordExporter(endpoint, region, serviceName, credentialsProvider, compression, parentExporter)
}
