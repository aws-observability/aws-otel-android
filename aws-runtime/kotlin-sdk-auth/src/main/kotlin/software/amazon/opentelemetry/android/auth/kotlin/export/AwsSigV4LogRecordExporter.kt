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
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.common.export.MemoryMode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import kotlinx.coroutines.runBlocking
import software.amazon.opentelemetry.android.auth.kotlin.AwsRequestSigner
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

/**
 * This exporter extends the functionality of LogRecordExporter and OtlpHttpLogRecordExporter to allow
 * exporting log records over OTLP with AWS SigV4 signing headers.
 */
class AwsSigV4LogRecordExporter(
    private val endpoint: String,
    private val region: String,
    private val serviceName: String,
    private val credentialsProvider: CredentialsProvider,
    private val compression: String,
    defaultParentExporter: LogRecordExporter? = null,
) : LogRecordExporter {
    companion object {
        fun builder(): AwsSigV4LogRecordExporterBuilder = AwsSigV4LogRecordExporterBuilder()
    }

    private val parentExporter: LogRecordExporter = defaultParentExporter ?: createDefaultExporter()

    private val logData: AtomicReference<Collection<LogRecordData>> = AtomicReference(Collections.emptyList())

    override fun export(logs: MutableCollection<LogRecordData>): CompletableResultCode {
        logData.set(logs)
        return parentExporter.export(logs)
    }

    override fun flush(): CompletableResultCode = parentExporter.flush()

    override fun shutdown(): CompletableResultCode = parentExporter.shutdown()

    private fun createDefaultExporter(): LogRecordExporter =
        OtlpHttpLogRecordExporter
            .builder()
            .setMemoryMode(MemoryMode.IMMUTABLE_DATA)
            .setEndpoint(endpoint)
            .setHeaders(AwsSigV4AuthHeaderSupplier())
            .setCompression(compression)
            .build()

    inner class AwsSigV4AuthHeaderSupplier : Supplier<Map<String, String>> {
        override fun get(): Map<String, String> {
            val logs = logData.get()
            val encodedLogs = ByteArrayOutputStream()
            LogsRequestMarshaler.create(logs).writeBinaryTo(encodedLogs)

            return runBlocking {
                AwsRequestSigner.getSignedHeaders(
                    encodedLogs.toByteArray(),
                    credentialsProvider,
                    serviceName,
                    endpoint,
                    region,
                )
            }
        }
    }
}
