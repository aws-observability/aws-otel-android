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

import android.os.Looper
import android.util.Log
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.AwsRumAppMonitorConfig
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.TelemetryConfig
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporter
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporterBuilder
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporter
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporterBuilder
import java.time.Duration

@ExtendWith(MockKExtension::class)
class RumAgentProviderTest {
    @MockK
    private lateinit var mockBuilder: OpenTelemetryAgent.Builder

    @MockK
    private lateinit var mockOpenTelemetryAgent: OpenTelemetryAgent

    @MockK
    private lateinit var mockDefaultSpanExporterBuilder: OtlpHttpSpanExporterBuilder

    @MockK
    private lateinit var mockDefaultLogRecordExporterBuilder: OtlpHttpLogRecordExporterBuilder

    @MockK
    private lateinit var mockAuthSpanExporterBuilder: AwsSigV4SpanExporterBuilder

    @MockK
    private lateinit var mockAuthLogRecordExporterBuilder: AwsSigV4LogRecordExporterBuilder

    private lateinit var rumAgentProvider: RumAgentProvider

    @BeforeEach
    fun setUp() {
        rumAgentProvider = RumAgentProvider()
        every { mockBuilder.build() } returns mockOpenTelemetryAgent
        every { mockBuilder.setAppMonitorConfig(any()) } returns mockBuilder
        every { mockBuilder.setSessionInactivityTimeout(any()) } returns mockBuilder
        every { mockBuilder.setEnabledTelemetry(any()) } returns mockBuilder
        every { mockBuilder.addSpanExporterCustomizer(any()) } returns mockBuilder
        every { mockBuilder.addLogRecordExporterCustomizer(any()) } returns mockBuilder

        mockkObject(AwsConfigReader)

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 1
        every { Log.w(any(), any<String>()) } returns 1
        every { Log.i(any(), any()) } returns 1
        every { Log.d(any(), any()) } returns 1

        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk()

        mockkStatic(OtlpHttpSpanExporter::class)
        every { OtlpHttpSpanExporter.builder() } returns mockDefaultSpanExporterBuilder
        mockkStatic(OtlpHttpLogRecordExporter::class)
        every { OtlpHttpLogRecordExporter.builder() } returns mockDefaultLogRecordExporterBuilder
        every { mockDefaultSpanExporterBuilder.setEndpoint(any()) } returns mockDefaultSpanExporterBuilder
        every { mockDefaultSpanExporterBuilder.build() } returns mockk()
        every { mockDefaultLogRecordExporterBuilder.setEndpoint(any()) } returns mockDefaultLogRecordExporterBuilder
        every { mockDefaultLogRecordExporterBuilder.build() } returns mockk()

        mockkObject(AwsSigV4SpanExporter)
        every { AwsSigV4SpanExporter.builder() } returns mockAuthSpanExporterBuilder
        mockkObject(AwsSigV4LogRecordExporter)
        every { AwsSigV4LogRecordExporter.builder() } returns mockAuthLogRecordExporterBuilder
        every { mockAuthSpanExporterBuilder.setRegion(any()) } returns mockAuthSpanExporterBuilder
        every { mockAuthSpanExporterBuilder.setEndpoint(any()) } returns mockAuthSpanExporterBuilder
        every { mockAuthSpanExporterBuilder.setServiceName(any()) } returns mockAuthSpanExporterBuilder
        every { mockAuthSpanExporterBuilder.setCredentialsProvider(any()) } returns mockAuthSpanExporterBuilder
        every { mockAuthSpanExporterBuilder.build() } returns mockk()
        every { mockAuthLogRecordExporterBuilder.setRegion(any()) } returns mockAuthLogRecordExporterBuilder
        every { mockAuthLogRecordExporterBuilder.setEndpoint(any()) } returns mockAuthLogRecordExporterBuilder
        every { mockAuthLogRecordExporterBuilder.setServiceName(any()) } returns mockAuthLogRecordExporterBuilder
        every { mockAuthLogRecordExporterBuilder.setCredentialsProvider(any()) } returns mockAuthLogRecordExporterBuilder
        every { mockAuthLogRecordExporterBuilder.build() } returns mockk()
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AwsConfigReader)
    }

    @Test
    fun `initialize should configure basic AwsRumAppMonitorConfig`() {
        // Given
        val config = createBasicConfig()
        val appMonitorConfigSlot = slot<AwsRumAppMonitorConfig>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setAppMonitorConfig(capture(appMonitorConfigSlot)) }
        val capturedConfig = appMonitorConfigSlot.captured
        assert(capturedConfig.region == "us-east-1")
        assert(capturedConfig.appMonitorId == "test-app-monitor-id")
        assert(capturedConfig.alias == "test-alias")
    }

    @Test
    fun `initialize should set session timeout from config`() {
        // Given
        val config = createBasicConfig(sessionTimeout = 600)
        val timeoutSlot = slot<Duration>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setSessionInactivityTimeout(capture(timeoutSlot)) }
        assert(timeoutSlot.captured == Duration.ofSeconds(600))
    }

    @Test
    fun `initialize should configure default exporters when cognito not configured`() {
        // Given
        val config = createBasicConfig(cognitoIdentityPoolId = null)
        val tracesEndpoint = "https://traces.endpoint.com"
        val logsEndpoint = "https://logs.endpoint.com"
        every { AwsConfigReader.getTracesEndpoint(config) } returns tracesEndpoint
        every { AwsConfigReader.getLogsEndpoint(config) } returns logsEndpoint

        val spanCustomizerSlot = slot<(SpanExporter) -> SpanExporter>()
        val logCustomizerSlot = slot<(LogRecordExporter) -> LogRecordExporter>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.addSpanExporterCustomizer(capture(spanCustomizerSlot)) }
        verify { mockBuilder.addLogRecordExporterCustomizer(capture(logCustomizerSlot)) }

        // Validate that the customizers create the correct exporter types
        val spanExporter = spanCustomizerSlot.captured.invoke(mockk())
        val logExporter = logCustomizerSlot.captured.invoke(mockk())

        Assertions.assertTrue(spanExporter is OtlpHttpSpanExporter)
        Assertions.assertTrue(logExporter is OtlpHttpLogRecordExporter)
    }

    @Test
    fun `initialize should configure default exporters when cognito IS configured`() {
        // Given
        val config = createBasicConfig(cognitoIdentityPoolId = "test-cognito-123")
        val tracesEndpoint = "https://traces.endpoint.com"
        val logsEndpoint = "https://logs.endpoint.com"
        every { AwsConfigReader.getTracesEndpoint(config) } returns tracesEndpoint
        every { AwsConfigReader.getLogsEndpoint(config) } returns logsEndpoint

        val spanCustomizerSlot = slot<(SpanExporter) -> SpanExporter>()
        val logCustomizerSlot = slot<(LogRecordExporter) -> LogRecordExporter>()

        mockkObject(CognitoIdentityClient)
        every { CognitoIdentityClient.invoke(any()) } returns mockk<CognitoIdentityClient>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.addSpanExporterCustomizer(capture(spanCustomizerSlot)) }
        verify { mockBuilder.addLogRecordExporterCustomizer(capture(logCustomizerSlot)) }

        // Validate that the customizers create the correct exporter types
        val spanExporter = spanCustomizerSlot.captured.invoke(mockk())
        val logExporter = logCustomizerSlot.captured.invoke(mockk())

        Assertions.assertTrue(spanExporter is AwsSigV4SpanExporter)
        Assertions.assertTrue(logExporter is AwsSigV4LogRecordExporter)
    }

    @Test
    fun `initialize should enable all telemetry when all are configured as enabled`() {
        // Given
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = true),
                anr = TelemetryOption(enabled = true),
                crash = TelemetryOption(enabled = true),
                fragment = TelemetryOption(enabled = true),
                network = TelemetryOption(enabled = true),
                slowRendering = TelemetryOption(enabled = true),
                startup = TelemetryOption(enabled = true),
                httpUrlConnection = TelemetryOption(enabled = true),
                okHttp3 = TelemetryOption(enabled = true),
                uiLoad = TelemetryOption(enabled = true),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        val telemetrySlot = slot<List<TelemetryConfig>>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setEnabledTelemetry(capture(telemetrySlot)) }
        val enabledTelemetries = telemetrySlot.captured
        assert(enabledTelemetries.size == 10)
        assert(enabledTelemetries.contains(TelemetryConfig.ACTIVITY))
        assert(enabledTelemetries.contains(TelemetryConfig.ANR))
        assert(enabledTelemetries.contains(TelemetryConfig.CRASH))
        assert(enabledTelemetries.contains(TelemetryConfig.FRAGMENT))
        assert(enabledTelemetries.contains(TelemetryConfig.NETWORK))
        assert(enabledTelemetries.contains(TelemetryConfig.SLOW_RENDERING))
        assert(enabledTelemetries.contains(TelemetryConfig.STARTUP))
        assert(enabledTelemetries.contains(TelemetryConfig.HTTP_URLCONNECTION))
        assert(enabledTelemetries.contains(TelemetryConfig.OKHTTP_3))
        assert(enabledTelemetries.contains(TelemetryConfig.UI_LOADING))
    }

    @Test
    fun `initialize should enable only selected telemetry when partially configured`() {
        // Given
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = true),
                anr = TelemetryOption(enabled = false),
                crash = TelemetryOption(enabled = true),
                fragment = TelemetryOption(enabled = false),
                network = TelemetryOption(enabled = true),
                slowRendering = null, // not configured
                startup = null, // not configured
                httpUrlConnection = TelemetryOption(enabled = false),
                okHttp3 = TelemetryOption(enabled = true),
                uiLoad = TelemetryOption(enabled = false),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        val telemetrySlot = slot<List<TelemetryConfig>>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setEnabledTelemetry(capture(telemetrySlot)) }
        val enabledTelemetries = telemetrySlot.captured
        assert(enabledTelemetries.size == 4)
        assert(enabledTelemetries.contains(TelemetryConfig.ACTIVITY))
        assert(enabledTelemetries.contains(TelemetryConfig.CRASH))
        assert(enabledTelemetries.contains(TelemetryConfig.NETWORK))
        assert(enabledTelemetries.contains(TelemetryConfig.OKHTTP_3))
        assert(!enabledTelemetries.contains(TelemetryConfig.ANR))
        assert(!enabledTelemetries.contains(TelemetryConfig.FRAGMENT))
        assert(!enabledTelemetries.contains(TelemetryConfig.HTTP_URLCONNECTION))
        assert(!enabledTelemetries.contains(TelemetryConfig.UI_LOADING))
    }

    @Test
    fun `initialize should not set enabled telemetry when telemetry config is null`() {
        // Given
        val config = createBasicConfig(telemetryConfigs = null)

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify(exactly = 0) { mockBuilder.setEnabledTelemetry(any()) }
    }

    @Test
    fun `initialize should enable no telemetry when all are disabled`() {
        // Given
        val telemetryConfigs =
            TelemetryConfigs(
                activity = TelemetryOption(enabled = false),
                anr = TelemetryOption(enabled = false),
                crash = TelemetryOption(enabled = false),
                fragment = TelemetryOption(enabled = false),
                network = TelemetryOption(enabled = false),
                slowRendering = TelemetryOption(enabled = false),
                startup = TelemetryOption(enabled = false),
                httpUrlConnection = TelemetryOption(enabled = false),
                okHttp3 = TelemetryOption(enabled = false),
                uiLoad = TelemetryOption(enabled = false),
            )
        val config = createBasicConfig(telemetryConfigs = telemetryConfigs)
        val telemetrySlot = slot<List<TelemetryConfig>>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setEnabledTelemetry(capture(telemetrySlot)) }
        val enabledTelemetries = telemetrySlot.captured
        assert(enabledTelemetries.isEmpty())
    }

    @Test
    fun `initialize should call build on builder`() {
        // Given
        val config = createBasicConfig()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.build() }
    }

    @Test
    fun `initialize should handle null rumAlias in config`() {
        // Given
        val awsConfig =
            AwsConfig(
                region = "us-east-1",
                rumAppMonitorId = "test-app-monitor-id",
                rumAlias = null,
            )
        val config = AgentConfig(aws = awsConfig)
        val appMonitorConfigSlot = slot<AwsRumAppMonitorConfig>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setAppMonitorConfig(capture(appMonitorConfigSlot)) }
        val capturedConfig = appMonitorConfigSlot.captured
        assert(capturedConfig.alias == null)
    }

    @Test
    fun `initialize should use default session timeout when not specified`() {
        // Given
        val config = createBasicConfig() // Uses default sessionTimeout = 300
        val timeoutSlot = slot<Duration>()

        // When
        rumAgentProvider.initialize(config, mockBuilder)

        // Then
        verify { mockBuilder.setSessionInactivityTimeout(capture(timeoutSlot)) }
        assert(timeoutSlot.captured == Duration.ofSeconds(300))
    }

    private fun createBasicConfig(
        region: String = "us-east-1",
        rumAppMonitorId: String = "test-app-monitor-id",
        rumAlias: String? = "test-alias",
        cognitoIdentityPoolId: String? = null,
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
                cognitoIdentityPoolId = cognitoIdentityPoolId,
            )
        return AgentConfig(
            aws = awsConfig,
            sessionTimeout = sessionTimeout,
            telemetry = telemetryConfigs,
            exportOverride = exportOverride,
            applicationAttributes = applicationAttributes,
        )
    }
}
