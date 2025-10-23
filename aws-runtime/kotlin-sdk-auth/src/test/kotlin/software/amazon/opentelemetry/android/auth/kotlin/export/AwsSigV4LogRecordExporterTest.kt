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

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.auth.kotlin.AuthTestBase

@ExtendWith(MockKExtension::class)
class AwsSigV4LogRecordExporterTest : AuthTestBase() {
    @MockK
    private lateinit var mockParentExporter: LogRecordExporter

    private lateinit var logRecordExporter: AwsSigV4LogRecordExporter

    @BeforeEach
    fun setup() {
        logRecordExporter =
            AwsSigV4LogRecordExporter
                .builder()
                .setEndpoint("dataplane.rum.us-east-1.amazonaws.com")
                .setRegion("us-east-1")
                .setCredentialsProvider(credentialsProvider)
                .setServiceName("rum")
                .setCompression("gzip")
                .setParentExporter(mockParentExporter)
                .build()

        every { mockParentExporter.export(any()) } returns CompletableResultCode.ofSuccess()
    }

    @Test
    fun `export with single mocked logrecord`() {
        val logData =
            mutableListOf(
                mockk<LogRecordData>(),
            )
        val result = logRecordExporter.export(logData)

        Assertions.assertTrue(result.isSuccess)
        Assertions.assertEquals(CompletableResultCode.ofSuccess(), result)
        verify { mockParentExporter.export(logData) }
    }

    @Test
    fun `export with parent exporter failing`() {
        every { mockParentExporter.export(any()) } returns CompletableResultCode.ofFailure()

        val result = logRecordExporter.export(mutableListOf())

        Assertions.assertFalse(result.isSuccess)
        Assertions.assertEquals(CompletableResultCode.ofFailure(), result)
    }
}
