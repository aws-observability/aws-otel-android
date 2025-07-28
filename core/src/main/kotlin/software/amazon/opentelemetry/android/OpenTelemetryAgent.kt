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
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.sdk.trace.samplers.Sampler
import software.amazon.opentelemetry.android.features.AttributesProvidingFeature
import software.amazon.opentelemetry.android.features.SessionIdTimeoutHandler
import software.amazon.opentelemetry.android.features.SessionManager
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
        private var enabledTelemetry: MutableList<TelemetryConfig>? = null
        private var enabledFeatures: MutableList<FeatureConfig>? = null

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

            if (tracerSampler != null) {
                delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
                    tracerSampler?.let { tracerProviderBuilder.setSampler(it) }
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
