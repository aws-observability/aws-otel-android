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
import android.util.Log
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.OpenTelemetryRumBuilder
import io.opentelemetry.android.SessionIdRatioBasedSampler
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
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
 * The main entrypoint for RUM OpenTelemetry on AWS.
 */
class OpenTelemetryAgent(
    private val delegate: OpenTelemetryRum,
) : OpenTelemetryRum {
    companion object {
        @Volatile
        private var openTelemetryAgent: OpenTelemetryAgent? = null

        @Synchronized
        fun setInstance(agent: OpenTelemetryAgent) {
            openTelemetryAgent = agent
        }

        fun getOpenTelemetryAgent(): OpenTelemetryAgent? = openTelemetryAgent

        const val TAG = "OpenTelemetryAgent"
    }

    override fun getOpenTelemetry(): OpenTelemetry = delegate.openTelemetry

    override fun getRumSessionId(): String = delegate.rumSessionId

    override fun emitEvent(
        eventName: String,
        body: String,
        attributes: Attributes,
    ) {
        delegate.emitEvent(eventName, body, attributes)
    }

    override fun shutdown() { }

    class Builder constructor(
        private val application: Application,
    ) {
        private var diskBufferingConfig: DiskBufferingConfig =
            DiskBufferingConfig(
                enabled = true,
                maxCacheSize = 10_000_000,
            )
        private var awsRumAppMonitorConfig: AwsRumAppMonitorConfig? = null
        private var applicationName: String? = null
        private val spanExporterCustomizers: MutableList<(SpanExporter) -> SpanExporter> = mutableListOf()
        private val logRecordExporterCustomizers: MutableList<(LogRecordExporter) -> LogRecordExporter> = mutableListOf()
        private var sessionInactivityTimeout: Duration = Duration.ofMinutes(5)
        private var tracerSampler: Sampler? = null
        private var sessionSampleRate: Double = 1.0
        private var enabledTelemetry: MutableList<TelemetryConfig>? = null
        private var enabledFeatures: MutableList<FeatureConfig>? = null
        private var customApplicationAttributes: Map<String, String>? = null
        private var capturedRequestHeaders: List<String>? = null
        private var capturedResponseHeaders: List<String>? = null

        /**
         * Point the Agent to an AppMonitor resource in AWS Real User Monitoring.
         */
        fun setAppMonitorConfig(config: AwsRumAppMonitorConfig): Builder {
            awsRumAppMonitorConfig = config
            return this
        }

        /**
         * Configure a custom application name to send with your spans and logs, otherwise use the
         * Android Application ContextWrapper to source the name
         */
        fun setApplicationName(config: String): Builder {
            applicationName = config
            return this
        }

        /**
         * Configure a custom DiskBufferingConfig is necessary
         */
        fun setDiskBufferingConfig(config: DiskBufferingConfig): Builder {
            diskBufferingConfig = config
            return this
        }

        /**
         * Add a function to invoke with the default SpanExporter to allow customization.
         */
        fun addSpanExporterCustomizer(spanExporterCustomizer: (SpanExporter) -> SpanExporter): Builder {
            spanExporterCustomizers.add(spanExporterCustomizer)
            return this
        }

        /**
         * Add a function to invoke with the default LogRecordExporter to allow customization
         */
        fun addLogRecordExporterCustomizer(logRecordExporterCustomizer: (LogRecordExporter) -> LogRecordExporter): Builder {
            logRecordExporterCustomizers.add(logRecordExporterCustomizer)
            return this
        }

        /**
         * Set the session background inactivity timeout
         */
        fun setSessionInactivityTimeout(timeout: Duration): Builder {
            sessionInactivityTimeout = timeout
            return this
        }

        /**
         * Define the trace sampler for SdkTracerProvider
         */
        fun setTracerSampler(sampler: Sampler): Builder {
            tracerSampler = sampler
            return this
        }

        fun setEnabledTelemetry(telemetries: List<TelemetryConfig>): Builder {
            enabledTelemetry = telemetries.toMutableList()
            return this
        }

        fun addEnabledTelemetry(telemetry: TelemetryConfig): Builder {
            if (enabledTelemetry == null) {
                enabledTelemetry = mutableListOf(telemetry)
            } else {
                enabledTelemetry!!.add(telemetry)
            }
            return this
        }

        fun setEnabledFeatures(features: List<FeatureConfig>): Builder {
            enabledFeatures = features.toMutableList()
            return this
        }

        fun addEnabledFeature(feature: FeatureConfig): Builder {
            if (enabledFeatures == null) {
                enabledFeatures = mutableListOf(feature)
            } else {
                enabledFeatures!!.add(feature)
            }
            return this
        }

        fun setCustomApplicationAttributes(attributes: Map<String, String>): Builder {
            this.customApplicationAttributes = attributes
            return this
        }

        fun setSessionSampleRate(rate: Double): Builder {
            if (rate < 0.0 || rate > 1.0) {
                Log.w(TAG, "Discarding invalid session sample rate: $rate")
                return this
            }
            this.sessionSampleRate = rate
            return this
        }

        fun setCapturedRequestHeaders(headers: List<String>): Builder {
            this.capturedRequestHeaders = headers
            return this
        }

        fun setCapturedResponseHeaders(headers: List<String>): Builder {
            this.capturedResponseHeaders = headers
            return this
        }

        fun build(): OpenTelemetryAgent {
            if (awsRumAppMonitorConfig == null) {
                throw IllegalStateException("Cannot build OpenTelemetryAgent without an AwsRumAppMonitorConfig")
            }

            val resource = ResourceProvider.createDefault(application, awsRumAppMonitorConfig!!, applicationName)

            val sessionProvider =
                SessionManager(
                    sessionIdTimeoutHandler =
                        SessionIdTimeoutHandler(
                            sessionInactivityTimeout = sessionInactivityTimeout,
                        ),
                )

            val otelRumConfig =
                OtelRumConfig()
                    .setDiskBufferingConfig(diskBufferingConfig)
                    .disableInstrumentationDiscovery()

            val telemetry =
                if (enabledTelemetry == null) {
                    TelemetryConfig.getDefault().toMutableList()
                } else {
                    enabledTelemetry!!
                }

            val features =
                if (enabledFeatures == null) {
                    FeatureConfig.getDefault().toMutableList()
                } else {
                    enabledFeatures!!
                }

            if (telemetry.find { it.configFlag == TelemetryConfig.SDK_INITIALIZATION_EVENTS.configFlag } == null) {
                otelRumConfig.disableSdkInitializationEvents()
            }

            // Set captured request/response headers
            val responseHeaders = capturedResponseHeaders?.toMutableList()
            val requestHeaders = capturedRequestHeaders?.toMutableList()
            if (responseHeaders != null || requestHeaders != null) {
                telemetry.forEach { telemetry ->
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

            // Special attributes from AttributesProvidingFeatures
            val globalAttributesBuilder = Attributes.builder()

            features.forEach { config ->
                config.feature?.install(application, application.applicationContext)
                if (config.feature is AttributesProvidingFeature) {
                    config.feature.buildAttributes().forEach { (key, value) ->
                        globalAttributesBuilder.put(key, value)
                    }
                }
            }

            customApplicationAttributes?.forEach { (attribute, value) ->
                globalAttributesBuilder.put(attribute, value)
            }

            val attributes = globalAttributesBuilder.build()
            if (!attributes.isEmpty) {
                otelRumConfig.setGlobalAttributes(attributes)
            }

            val delegateBuilder =
                OpenTelemetryRumBuilder
                    .create(application, otelRumConfig)
                    .setSessionProvider(sessionProvider)
                    .setResource(resource)

            spanExporterCustomizers.forEach { customizer ->
                delegateBuilder.addSpanExporterCustomizer(customizer)
            }
            logRecordExporterCustomizers.forEach { customizer ->
                delegateBuilder.addLogRecordExporterCustomizer(customizer)
            }

            delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
                tracerSampler?.let { tracerProviderBuilder.setSampler(it) }
                tracerProviderBuilder.addSpanProcessor(CpuAttributesSpanProcessor())
            }

            // Add a SessionIdRatioBasedSampler if we have a sessionSampleRate < 1.0
            if (sessionSampleRate < 1.0) {
                val sampler = SessionIdRatioBasedSampler(sessionSampleRate, sessionProvider)
                delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
                    tracerProviderBuilder.setSampler(sampler)
                }
            }

            // Add all enabled telemetry instrumentations
            telemetry.forEach { telemetryConfig ->
                telemetryConfig.instrumentation?.let {
                    delegateBuilder.addInstrumentation(telemetryConfig.instrumentation)
                }
            }

            val openTelemetryAgent = OpenTelemetryAgent(delegateBuilder.build())
            setInstance(openTelemetryAgent)
            return openTelemetryAgent
        }
    }
}
