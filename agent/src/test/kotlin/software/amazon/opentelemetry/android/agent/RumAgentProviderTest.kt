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
package software.amazon.opentelemetry.android.agent

import android.app.Application
import android.os.Looper
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.OpenTelemetryRumClientConfig
import software.amazon.opentelemetry.android.TelemetryConfig
import java.time.Duration

@ExtendWith(MockKExtension::class)
class RumAgentProviderTest {
    @MockK
    private lateinit var mockApplication: Application

    @MockK
    private lateinit var mockSpanExporterBuilder: OtlpHttpSpanExporterBuilder

    @MockK
    private lateinit var mockLogRecordExporterBuilder: OtlpHttpLogRecordExporterBuilder

    @MockK
    private lateinit var mockSpanExporter: OtlpHttpSpanExporter

    @MockK
    private lateinit var mockLogRecordExporter: OtlpHttpLogRecordExporter

    private lateinit var rumAgentProvider: RumAgentProvider
    private lateinit var capturedConfig: OpenTelemetryRumClientConfig

    @BeforeEach
    fun setUp() {
        rumAgentProvider = RumAgentProvider()

        mockkObject(AwsConfigReader)
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 1
        every { Log.w(any(), any<String>()) } returns 1
        every { Log.i(any(), any()) } returns 1
        every { Log.d(any(), any()) } returns 1

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()

        mockkStatic(OtlpHttpSpanExporter::class)
        every { OtlpHttpSpanExporter.builder() } returns mockSpanExporterBuilder
        every { mockSpanExporterBuilder.setEndpoint(any()) } returns mockSpanExporterBuilder
        every { mockSpanExporterBuilder.setCompression(any()) } returns mockSpanExporterBuilder
        every { mockSpanExporterBuilder.build() } returns mockSpanExporter

        mockkStatic(OtlpHttpLogRecordExporter::class)
        every { OtlpHttpLogRecordExporter.builder() } returns mockLogRecordExporterBuilder
        every { mockLogRecordExporterBuilder.setEndpoint(any()) } returns mockLogRecordExporterBuilder
        every { mockLogRecordExporterBuilder.setCompression(any()) } returns mockLogRecordExporterBuilder
        every { mockLogRecordExporterBuilder.build() } returns mockLogRecordExporter

        mockkObject(OpenTelemetryRumClient)
        every { OpenTelemetryRumClient.invoke(any()) } answers {
            val block = firstArg<OpenTelemetryRumClientConfig.() -> Unit>()
            capturedConfig = OpenTelemetryRumClientConfig().apply(block)
            mockk(relaxed = true)
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initialize should configure basic AWS RUM settings`() {
        val config = createBasicConfig()
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals("us-east-1", capturedConfig.awsRumConfig?.region)
        assertEquals("test-app-monitor-id", capturedConfig.awsRumConfig?.appMonitorId)
        assertEquals("test-alias", capturedConfig.awsRumConfig?.alias)
        assertEquals(mockApplication, capturedConfig.androidApplication)
    }

    @Test
    fun `initialize should set session timeout from config`() {
        val config = createBasicConfig(sessionTimeout = 600)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(Duration.ofSeconds(600), capturedConfig.sessionInactivityTimeout)
    }

    @Test
    fun `initialize should configure exporters with correct endpoints`() {
        val config = createBasicConfig()
        val tracesEndpoint = "https://traces.endpoint.com"
        val logsEndpoint = "https://logs.endpoint.com"
        every { AwsConfigReader.getTracesEndpoint(config) } returns tracesEndpoint
        every { AwsConfigReader.getLogsEndpoint(config) } returns logsEndpoint

        rumAgentProvider.initialize(config, mockApplication)

        verify { mockSpanExporterBuilder.setEndpoint(tracesEndpoint) }
        verify { mockLogRecordExporterBuilder.setEndpoint(logsEndpoint) }
        assertEquals(mockSpanExporter, capturedConfig.spanExporter)
        assertEquals(mockLogRecordExporter, capturedConfig.logRecordExporter)
    }

    @Test
    fun `initialize should enable all telemetry when all are configured as enabled`() {
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = true),
                anr = TelemetryOption(enabled = true),
                crash = TelemetryOption(enabled = true),
                fragment = TelemetryOption(enabled = true),
                network = TelemetryOption(enabled = true),
                slowRendering = TelemetryOption(enabled = true),
                startup = TelemetryOption(enabled = true),
                http = HttpTelemetryOption(enabled = true),
                uiLoad = TelemetryOption(enabled = true),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        val enabledTelemetries = capturedConfig.telemetry!!
        assertEquals(10, enabledTelemetries.size)
        assertTrue(enabledTelemetries.contains(TelemetryConfig.ACTIVITY))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.ANR))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.CRASH))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.FRAGMENT))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.NETWORK))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.SLOW_RENDERING))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.STARTUP))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.HTTP_URLCONNECTION))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.OKHTTP_3))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.UI_LOADING))
    }

    @Test
    fun `initialize should enable only selected telemetry when partially configured`() {
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = true),
                anr = TelemetryOption(enabled = false),
                crash = TelemetryOption(enabled = true),
                fragment = TelemetryOption(enabled = false),
                network = TelemetryOption(enabled = true),
                slowRendering = null,
                startup = null,
                http = HttpTelemetryOption(enabled = false),
                uiLoad = TelemetryOption(enabled = false),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        val enabledTelemetries = capturedConfig.telemetry!!
        assertEquals(3, enabledTelemetries.size)
        assertTrue(enabledTelemetries.contains(TelemetryConfig.ACTIVITY))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.CRASH))
        assertTrue(enabledTelemetries.contains(TelemetryConfig.NETWORK))
    }

    @Test
    fun `initialize should not set telemetry when config is null`() {
        val config = createBasicConfig(telemetryConfigs = null)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(null, capturedConfig.telemetry)
    }

    @Test
    fun `initialize should enable no telemetry when all are disabled`() {
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = false),
                anr = TelemetryOption(enabled = false),
                crash = TelemetryOption(enabled = false),
                fragment = TelemetryOption(enabled = false),
                network = TelemetryOption(enabled = false),
                slowRendering = TelemetryOption(enabled = false),
                startup = TelemetryOption(enabled = false),
                http = HttpTelemetryOption(enabled = false),
                uiLoad = TelemetryOption(enabled = false),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertTrue(capturedConfig.telemetry!!.isEmpty())
    }

    @Test
    fun `initialize should handle null rumAlias in config`() {
        val awsConfig =
            AwsConfig(
                region = "us-east-1",
                rumAppMonitorId = "test-app-monitor-id",
                rumAlias = null,
            )
        val config = AgentConfig(aws = awsConfig)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(null, capturedConfig.awsRumConfig?.alias)
    }

    @Test
    fun `initialize should use default session timeout when not specified`() {
        val config = createBasicConfig()
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(Duration.ofSeconds(300), capturedConfig.sessionInactivityTimeout)
    }

    @Test
    fun `initialize should use configured compression for exporters`() {
        val exportOverride = ExportOverrideConfig(compression = "gzip")
        val config = createBasicConfig(exportOverride = exportOverride)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        verify { mockSpanExporterBuilder.setCompression("gzip") }
        verify { mockLogRecordExporterBuilder.setCompression("gzip") }
    }

    @Test
    fun `initialize should use default compression when not configured`() {
        val config = createBasicConfig()
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        verify { mockSpanExporterBuilder.setCompression(RumAgentProvider.DEFAULT_COMPRESSION) }
        verify { mockLogRecordExporterBuilder.setCompression(RumAgentProvider.DEFAULT_COMPRESSION) }
    }

    @Test
    fun `initialize should set session sample rate`() {
        val config = createBasicConfig()
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(config.sessionSampleRate, capturedConfig.sessionSampleRate)
    }

    @Test
    fun `initialize should set otel resource attributes when provided`() {
        val attributes =
            mapOf(
                "custom.attribute" to JsonPrimitive("value"),
            )
        val config = createBasicConfig(applicationAttributes = attributes)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals("value", capturedConfig.otelResource!!.getAttribute(AttributeKey.stringKey("custom.attribute")))
    }

    @Test
    fun `initialize should set captured request and response headers when http telemetry enabled`() {
        val telemetryConfigs =
            TelemetryConfigs(
                http =
                    HttpTelemetryOption(
                        enabled = true,
                        capturedRequestHeaders = listOf("Authorization", "Content-Type"),
                        capturedResponseHeaders = listOf("X-Custom-Header"),
                    ),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        every { AwsConfigReader.getTracesEndpoint(config) } returns "https://traces.endpoint.com"
        every { AwsConfigReader.getLogsEndpoint(config) } returns "https://logs.endpoint.com"

        rumAgentProvider.initialize(config, mockApplication)

        assertEquals(listOf("Authorization", "Content-Type"), capturedConfig.capturedRequestHeaders)
        assertEquals(listOf("X-Custom-Header"), capturedConfig.capturedResponseHeaders)
    }

    private fun createBasicConfig(
        region: String = "us-east-1",
        rumAppMonitorId: String = "test-app-monitor-id",
        rumAlias: String? = "test-alias",
        sessionTimeout: Int = 300,
        telemetryConfigs: TelemetryConfigs? = null,
        exportOverride: ExportOverrideConfig? = null,
        applicationAttributes: Map<String, JsonPrimitive>? = null,
    ): AgentConfig {
        val awsConfig =
            AwsConfig(
                region = region,
                rumAppMonitorId = rumAppMonitorId,
                rumAlias = rumAlias,
            )
        return AgentConfig(
            aws = awsConfig,
            sessionTimeout = sessionTimeout,
            telemetry = telemetryConfigs,
            exportOverride = exportOverride,
            otelResourceAttributes = applicationAttributes,
        )
    }
}
