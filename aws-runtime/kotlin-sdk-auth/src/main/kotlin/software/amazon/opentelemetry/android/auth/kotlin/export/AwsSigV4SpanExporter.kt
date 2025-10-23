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
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.common.export.MemoryMode
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import kotlinx.coroutines.runBlocking
import software.amazon.opentelemetry.android.auth.kotlin.AwsRequestSigner
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

/**
 * This exporter extends the functionality of SpanExporter and OtlpHttpSpanExporter to allow exporting
 * spans over OTLP with AWS SigV4 signing headers.
 */
class AwsSigV4SpanExporter(
    private val endpoint: String,
    private val region: String,
    private val serviceName: String,
    private val credentialsProvider: CredentialsProvider,
    private val compression: String,
    defaultParentExporter: SpanExporter? = null,
) : SpanExporter {
    companion object {
        fun builder(): AwsSigV4SpanExporterBuilder = AwsSigV4SpanExporterBuilder()
    }

    private val parentExporter: SpanExporter = defaultParentExporter ?: createDefaultExporter()

    private val spanData: AtomicReference<Collection<SpanData>> = AtomicReference(Collections.emptyList())

    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        spanData.set(spans)
        return parentExporter.export(spans)
    }

    override fun flush(): CompletableResultCode = parentExporter.flush()

    override fun shutdown(): CompletableResultCode = parentExporter.shutdown()

    private fun createDefaultExporter(): SpanExporter =
        OtlpHttpSpanExporter
            .builder()
            .setMemoryMode(MemoryMode.IMMUTABLE_DATA)
            .setEndpoint(endpoint)
            .setHeaders(AwsSigV4AuthHeaderSupplier())
            .setCompression(compression)
            .build()

    /**
     * Provides Sigv4 headers for the OtlpHttpSpanExporter
     */
    inner class AwsSigV4AuthHeaderSupplier : Supplier<Map<String, String>> {
        override fun get(): Map<String, String> {
            val spans = spanData.get()
            val encodedSpans = ByteArrayOutputStream()
            TraceRequestMarshaler.create(spans).writeBinaryTo(encodedSpans)

            return runBlocking {
                AwsRequestSigner.getSignedHeaders(
                    encodedSpans.toByteArray(),
                    credentialsProvider,
                    serviceName,
                    endpoint,
                    region,
                )
            }
        }
    }
}
