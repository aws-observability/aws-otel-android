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
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.OpenTelemetryRumBuilder
import io.opentelemetry.android.SessionIdRatioBasedSampler
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.instrumentation.library.httpurlconnection.HttpUrlInstrumentation
import io.opentelemetry.instrumentation.library.okhttp.v3_0.OkHttpInstrumentation
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.sdk.trace.samplers.Sampler
import software.amazon.opentelemetry.android.features.AttributesProvidingFeature
import software.amazon.opentelemetry.android.features.SessionIdTimeoutHandler
import software.amazon.opentelemetry.android.features.SessionManager
import software.amazon.opentelemetry.android.processor.CpuAttributesSpanProcessor
import java.time.Duration

/**
 * The main entrypoint for RUM OpenTelemetry on AWS with Kotlin DSL configuration.
 */
class OpenTelemetryRumClient internal constructor(
    private val delegate: OpenTelemetryRum,
) : OpenTelemetry by delegate.openTelemetry {
    companion object {
        @Volatile
        private var instance: OpenTelemetryRumClient? = null

        @Synchronized
        internal fun setInstance(client: OpenTelemetryRumClient) {
            instance = client
        }

        fun getInstance(): OpenTelemetryRumClient? = instance

        const val TAG = "OpenTelemetryRumClient"

        operator fun invoke(block: OpenTelemetryRumClientConfig.() -> Unit): OpenTelemetryRumClient =
            OpenTelemetryRumClientConfig().apply(block).build()

        const val DEFAULT_COMPRESSION = "none"
    }

    val openTelemetry: OpenTelemetry get() = delegate.openTelemetry
    val rumSessionId: String get() = delegate.rumSessionId

    fun emitEvent(
        eventName: String,
        body: String = "",
        attributes: Attributes = Attributes.empty(),
    ) {
        delegate.emitEvent(eventName, body, attributes)
    }

    fun shutdown() = delegate.shutdown()
}

/**
 * Configuration DSL for AWS RUM App Monitor
 */
class AwsRumConfig {
    var region: String = ""
    var appMonitorId: String = ""
    var alias: String? = null

    internal fun build(): AwsRumAppMonitorConfig {
        require(region.isNotBlank()) { "AWS region is required" }
        require(appMonitorId.isNotBlank()) { "AWS RUM App Monitor ID is required" }
        return AwsRumAppMonitorConfig(region, appMonitorId, alias)
    }
}

/**
 * Configuration DSL for disk buffering
 */
class DiskBufferingConfigDsl {
    var enabled: Boolean = true
    var maxCacheSize: Int = 10_000_000

    internal fun build(): DiskBufferingConfig = DiskBufferingConfig(enabled, maxCacheSize)
}

/**
 * Configuration DSL for OpenTelemetry RUM Client
 */
class OpenTelemetryRumClientConfig {
    var awsRumConfig: AwsRumConfig? = null
    var diskBufferingConfig: DiskBufferingConfigDsl = DiskBufferingConfigDsl()
    var tracerSampler: Sampler? = null

    lateinit var androidApplication: Application
    var spanExporter: SpanExporter? = null
    var logRecordExporter: LogRecordExporter? = null
    var sessionInactivityTimeout: Duration = Duration.ofMinutes(5)
    var sessionSampleRate: Double = 1.0
        set(value) {
            require(value in 0.0..1.0) { "Session sample rate must be between 0.0 and 1.0" }
            field = value
        }
    var telemetry: List<TelemetryConfig>? = null
    var features: List<FeatureConfig>? = null
    var applicationAttributes: Map<String, String> = emptyMap()
    var serviceVersion: String? = null
    var serviceName: String? = null
    var capturedRequestHeaders: List<String>? = null
    var capturedResponseHeaders: List<String>? = null

    /**
     * Configure AWS RUM App Monitor settings
     */
    fun awsRum(block: AwsRumConfig.() -> Unit) {
        awsRumConfig = AwsRumConfig().apply(block)
    }

    /**
     * Configure disk buffering settings
     */
    fun diskBuffering(block: DiskBufferingConfigDsl.() -> Unit) {
        diskBufferingConfig.apply(block)
    }

    /**
     * Set tracer sampler
     */
    fun tracerSampler(sampler: Sampler) {
        tracerSampler = sampler
    }

    fun build(): OpenTelemetryRumClient {
        val rumConfig =
            awsRumConfig?.build()
                ?: throw IllegalStateException("AWS RUM configuration is required. Use awsRum { } block.")

        val resource = ResourceProvider.createDefault(androidApplication, rumConfig, appVersion = serviceVersion, appName = serviceName)

        val sessionProvider =
            SessionManager(
                sessionIdTimeoutHandler =
                    SessionIdTimeoutHandler(
                        sessionInactivityTimeout = sessionInactivityTimeout,
                    ),
            )

        val otelRumConfig =
            OtelRumConfig()
                .setDiskBufferingConfig(diskBufferingConfig.build())
                .disableInstrumentationDiscovery()

        val enabledTelemetry = telemetry ?: TelemetryConfig.getDefault()
        val enabledFeatures = features ?: FeatureConfig.getDefault()

        if (enabledTelemetry.none { it.configFlag == TelemetryConfig.SDK_INITIALIZATION_EVENTS.configFlag }) {
            otelRumConfig.disableSdkInitializationEvents()
        }

        // Set captured request/response headers
        val responseHeaders = capturedResponseHeaders?.toMutableList()
        val requestHeaders = capturedRequestHeaders?.toMutableList()
        if (responseHeaders != null || requestHeaders != null) {
            enabledTelemetry.forEach { telemetry ->
                if (telemetry.instrumentation is HttpUrlInstrumentation) {
                    telemetry.instrumentation.capturedRequestHeaders = requestHeaders ?: mutableListOf()
                    telemetry.instrumentation.capturedResponseHeaders = responseHeaders ?: mutableListOf()
                }
                if (telemetry.instrumentation is OkHttpInstrumentation) {
                    telemetry.instrumentation.capturedRequestHeaders = requestHeaders ?: mutableListOf()
                    telemetry.instrumentation.capturedResponseHeaders = responseHeaders ?: mutableListOf()
                }
            }
        }

        // Build global attributes from features and custom attributes
        val globalAttributesBuilder = Attributes.builder()

        enabledFeatures.forEach { config ->
            config.feature?.install(androidApplication, androidApplication.applicationContext)
            if (config.feature is AttributesProvidingFeature) {
                config.feature.buildAttributes().forEach { (key, value) ->
                    globalAttributesBuilder.put(key, value)
                }
            }
        }

        applicationAttributes.forEach { (key, value) ->
            globalAttributesBuilder.put(key, value)
        }

        val globalAttributes = globalAttributesBuilder.build()
        if (!globalAttributes.isEmpty) {
            otelRumConfig.setGlobalAttributes(globalAttributes)
        }

        val delegateBuilder =
            OpenTelemetryRumBuilder
                .create(androidApplication, otelRumConfig)
                .setSessionProvider(sessionProvider)
                .setResource(resource)

        val defaultEndpoint = buildRumEndpoint(rumConfig.region)

        delegateBuilder.addSpanExporterCustomizer { _ ->
            if (spanExporter == null) {
                OtlpHttpSpanExporter
                    .builder()
                    .setEndpoint(defaultEndpoint)
                    .setCompression(OpenTelemetryRumClient.DEFAULT_COMPRESSION)
                    .build()
            } else {
                spanExporter
            }
        }

        delegateBuilder.addLogRecordExporterCustomizer { _ ->
            if (logRecordExporter == null) {
                OtlpHttpLogRecordExporter
                    .builder()
                    .setEndpoint(defaultEndpoint)
                    .setCompression(OpenTelemetryRumClient.DEFAULT_COMPRESSION)
                    .build()
            } else {
                logRecordExporter
            }
        }

        delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
            tracerSampler?.let { tracerProviderBuilder.setSampler(it) }
            tracerProviderBuilder.addSpanProcessor(CpuAttributesSpanProcessor())
        }

        // Add session-based sampling if needed
        if (sessionSampleRate < 1.0) {
            val sampler = SessionIdRatioBasedSampler(sessionSampleRate, sessionProvider)
            delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
                tracerProviderBuilder.setSampler(sampler)
            }
        }

        // Add enabled telemetry instrumentations
        enabledTelemetry.forEach { telemetryConfig ->
            telemetryConfig.instrumentation?.let {
                delegateBuilder.addInstrumentation(it)
            }
        }

        val client = OpenTelemetryRumClient(delegateBuilder.build())
        OpenTelemetryRumClient.setInstance(client)
        return client
    }

    internal fun buildRumEndpoint(region: String): String = "https://dataplane.rum.$region.amazonaws.com/v1/rum"
}
