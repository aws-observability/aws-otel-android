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
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.incubating.CloudIncubatingAttributes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.features.DiskManager
import software.amazon.opentelemetry.android.features.UserIdManager
import java.time.Duration

@ExtendWith(MockKExtension::class)
class OpenTelemetryAgentTest {
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

    private lateinit var openTelemetryAgent: OpenTelemetryAgent

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

        openTelemetryAgent = OpenTelemetryAgent(delegate)
    }

    @Test
    fun `getOpenTelemetry should pass a reference to delegate OpenTelemetry`() {
        val delegateOpenTelemetry = openTelemetryAgent.openTelemetry

        Assertions.assertEquals(openTelemetryNoOp, delegateOpenTelemetry)
        verify(exactly = 1) { delegate.getOpenTelemetry() }
    }

    @Test
    fun `getRumSessionId should pass a reference to delegate RumSessionId`() {
        val delegateRumSessionId = openTelemetryAgent.rumSessionId

        Assertions.assertEquals(sessionId, delegateRumSessionId)
        verify(exactly = 1) { delegate.getRumSessionId() }
    }

    @Test
    fun `builder should pass all functions to delegate builder`() {
        mockkStatic(OpenTelemetryRumBuilder::class)
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()

        val otelRumConfig = slot<OtelRumConfig>()

        val delegateBuilder = mockk<OpenTelemetryRumBuilder>(relaxed = true)
        every { OpenTelemetryRumBuilder.create(application, capture(otelRumConfig)) } returns delegateBuilder
        every { delegateBuilder.build() } returns openTelemetryAgent

        every { delegateBuilder.setResource(any()) } returns delegateBuilder
        every { delegateBuilder.addSpanExporterCustomizer(any()) } returns delegateBuilder
        every { delegateBuilder.addLogRecordExporterCustomizer(any()) } returns delegateBuilder

        val spanExporterCustomizer: (SpanExporter) -> SpanExporter = { spanExporter }
        val logExporterCustomizer: (LogRecordExporter) -> LogRecordExporter = { logRecordExporter }

        OpenTelemetryAgent
            .Builder(application)
            .setApplicationName("testAppName")
            .setApplicationVersion("1.0")
            .setAppMonitorConfig(
                AwsRumAppMonitorConfig(
                    region = "us-east-1",
                    appMonitorId = "1234",
                ),
            ).addSpanExporterCustomizer(spanExporterCustomizer)
            .addLogRecordExporterCustomizer(logExporterCustomizer)
            .setSessionInactivityTimeout(Duration.ofMinutes(1))
            .setEnabledTelemetry(listOf(TelemetryConfig.ACTIVITY, TelemetryConfig.ANR))
            .setEnabledFeatures(listOf(FeatureConfig.USER_ID))
            .build()

        // Validate the expected delegate builder
        verify(exactly = 1) {
            delegateBuilder.setResource(
                withArg {
                    Assertions.assertEquals("testAppName", it.getAttribute(ServiceAttributes.SERVICE_NAME))
                    Assertions.assertEquals(
                        "us-east-1",
                        it.getAttribute(AttributeKey.stringKey(CloudIncubatingAttributes.CLOUD_REGION.key)),
                    )
                    Assertions.assertEquals("1234", it.getAttribute(AttributeKey.stringKey(AwsRumAttributes.AWS_RUM_APP_MONITOR_ID)))
                },
            )
            delegateBuilder.addSpanExporterCustomizer(
                withArg {
                    Assertions.assertEquals(spanExporter, it.apply(spanExporter))
                },
            )
            delegateBuilder.addLogRecordExporterCustomizer(
                withArg {
                    Assertions.assertEquals(logRecordExporter, it.apply(logRecordExporter))
                },
            )
            delegateBuilder.addInstrumentation(TelemetryConfig.ACTIVITY.instrumentation!!)
            delegateBuilder.addInstrumentation(TelemetryConfig.ANR.instrumentation!!)
        }

        val attributes = otelRumConfig.captured.globalAttributesSupplier.get()
        Assertions.assertNotNull(attributes)

        val userIdAttribute = attributes.get(AttributeKey.stringKey(UserIdManager.USER_ID_ATTR))
        Assertions.assertNotNull(userIdAttribute)
        Assertions.assertEquals(userId, userIdAttribute)
    }

    @Test
    fun `builder should throw IllegalStateException if not all required fields are present`() {
        assertThrows<IllegalStateException> {
            OpenTelemetryAgent
                .Builder(application)
                .setApplicationName("testAppName")
                .build()
        }
    }
}
