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
package software.amazon.opentelemetry.android

import android.app.Application
import android.os.Looper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.OpenTelemetryRumBuilder
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.incubating.CloudIncubatingAttributes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.features.DiskManager
import software.amazon.opentelemetry.android.features.UserIdManager
import java.time.Duration

@ExtendWith(MockKExtension::class)
class OpenTelemetryRumClientTest {
    @MockK
    lateinit var delegate: OpenTelemetryRum

    @RelaxedMockK
    lateinit var application: Application

    @MockK
    lateinit var spanExporter: SpanExporter

    @MockK
    lateinit var logRecordExporter: LogRecordExporter

    @MockK
    private lateinit var diskManager: DiskManager

    private val openTelemetryNoOp: OpenTelemetry = OpenTelemetry.noop()
    private val sessionId = "testId"
    private val userId = "userId"

    @BeforeEach
    fun setup() {
        every { delegate.openTelemetry } returns openTelemetryNoOp
        every { delegate.rumSessionId } returns sessionId

        mockkObject(DiskManager.Companion)
        every { DiskManager.Companion.getInstance() } returns diskManager

        // Mock user id reads / writes
        every { diskManager.readFromFileIfExists(any(), UserIdManager.USER_ID_FILE) } returns userId
        every { diskManager.writeToFile(any(), UserIdManager.USER_ID_FILE, any()) } returns true
    }

    @Test
    fun `should validate AWS RUM configuration DSL`() {
        val config =
            AwsRumConfig().apply {
                region = "us-east-1"
                appMonitorId = "test-monitor-id"
                alias = "test-alias"
            }

        val rumConfig = config.build()
        assertEquals("us-east-1", rumConfig.region)
        assertEquals("test-monitor-id", rumConfig.appMonitorId)
        assertEquals("test-alias", rumConfig.alias)
    }

    @Test
    fun `should validate disk buffering configuration DSL`() {
        val config =
            DiskBufferingConfigDsl().apply {
                enabled = false
                maxCacheSize = 5_000_000
            }

        val diskConfig = config.build()
        assertEquals(false, diskConfig.enabled)
        assertEquals(5_000_000, diskConfig.maxCacheSize)
    }

    @Test
    fun `should throw exception when AWS region is blank`() {
        assertThrows<IllegalArgumentException> {
            AwsRumConfig()
                .apply {
                    region = ""
                    appMonitorId = "test-monitor-id"
                }.build()
        }
    }

    @Test
    fun `should throw exception when appMonitorId is blank`() {
        assertThrows<IllegalArgumentException> {
            AwsRumConfig()
                .apply {
                    region = "us-east-1"
                    appMonitorId = ""
                }.build()
        }
    }

    @Test
    fun `should validate session sample rate bounds`() {
        // Valid rates should not throw
        val validRates = listOf(0.0, 0.5, 1.0)
        validRates.forEach { rate ->
            // This should not throw
            require(rate in 0.0..1.0) { "Session sample rate must be between 0.0 and 1.0" }
        }

        // Invalid rates should throw
        assertThrows<IllegalArgumentException> {
            require(1.5 in 0.0..1.0) { "Session sample rate must be between 0.0 and 1.0" }
        }

        assertThrows<IllegalArgumentException> {
            require(-0.1 in 0.0..1.0) { "Session sample rate must be between 0.0 and 1.0" }
        }
    }

    @Test
    fun `getOpenTelemetry should pass a reference to delegate OpenTelemetry`() {
        val client = OpenTelemetryRumClient(delegate)
        val delegateOpenTelemetry = client.openTelemetry

        assertEquals(openTelemetryNoOp, delegateOpenTelemetry)
        verify(atLeast = 1) { delegate.openTelemetry }
    }

    @Test
    fun `builder should pass all functions to delegate builder`() {
        mockkStatic(OpenTelemetryRumBuilder::class)
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()

        val otelRumConfig = slot<OtelRumConfig>()

        val delegateBuilder = mockk<OpenTelemetryRumBuilder>(relaxed = true)
        every { OpenTelemetryRumBuilder.create(application, capture(otelRumConfig)) } returns delegateBuilder
        every { delegateBuilder.build() } returns delegate

        every { delegateBuilder.setResource(any()) } returns delegateBuilder
        every { delegateBuilder.setSessionProvider(any()) } returns delegateBuilder
        every { delegateBuilder.addSpanExporterCustomizer(any()) } returns delegateBuilder
        every { delegateBuilder.addLogRecordExporterCustomizer(any()) } returns delegateBuilder

        OpenTelemetryRumClient {
            androidApplication = application
            awsRum {
                region = "us-east-1"
                appMonitorId = "1234"
            }
            sessionInactivityTimeout = Duration.ofMinutes(1)
            telemetry = listOf(TelemetryConfig.ACTIVITY, TelemetryConfig.ANR)
            features = listOf(FeatureConfig.USER_ID)
            otelResource =
                Resource
                    .builder()
                    .put("app.test", "123")
                    .put("service.name", "testAppName")
                    .put("service.version", "1.0")
                    .build()
        }

        // Validate the expected delegate builder
        verify(exactly = 1) {
            delegateBuilder.setResource(
                withArg {
                    assertEquals("testAppName", it.getAttribute(ServiceAttributes.SERVICE_NAME))
                    assertEquals("1.0", it.getAttribute(ServiceAttributes.SERVICE_VERSION))
                    assertEquals(
                        "us-east-1",
                        it.getAttribute(AttributeKey.stringKey(CloudIncubatingAttributes.CLOUD_REGION.key)),
                    )
                    assertEquals("1234", it.getAttribute(AttributeKey.stringKey(AwsRumAttributes.AWS_RUM_APP_MONITOR_ID)))
                },
            )
            delegateBuilder.addSpanExporterCustomizer(
                withArg {
                    assertTrue(it.apply(spanExporter) is OtlpHttpSpanExporter)
                },
            )
            delegateBuilder.addLogRecordExporterCustomizer(
                withArg {
                    assertTrue(it.apply(logRecordExporter) is OtlpHttpLogRecordExporter)
                },
            )
            delegateBuilder.addInstrumentation(TelemetryConfig.ACTIVITY.instrumentation!!)
            delegateBuilder.addInstrumentation(TelemetryConfig.ANR.instrumentation!!)
        }

        val globalAttributes = otelRumConfig.captured.globalAttributesSupplier.get()
        assertNotNull(globalAttributes)

        val userIdAttribute = globalAttributes.get(AttributeKey.stringKey(UserIdManager.USER_ID_ATTR))
        assertNotNull(userIdAttribute)
        assertEquals(userId, userIdAttribute)

        assertEquals("123", globalAttributes.get(AttributeKey.stringKey("app.test")))
    }

    @Test
    fun `builder should throw IllegalStateException if not all required fields are present`() {
        assertThrows<IllegalStateException> {
            OpenTelemetryRumClient {
                application = this@OpenTelemetryRumClientTest.application
            }
        }
    }
}
