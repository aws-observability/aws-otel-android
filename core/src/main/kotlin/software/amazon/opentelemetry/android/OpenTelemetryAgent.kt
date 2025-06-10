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
import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.trace.export.SpanExporter
import io.opentelemetry.sdk.trace.samplers.Sampler
import software.amazon.opentelemetry.android.uiload.activity.ActivityLoadInstrumentation
import java.time.Duration

/**
 * The main entrypoint for RUM OpenTelemetry on AWS.
 */
class OpenTelemetryAgent(
    private val delegate: OpenTelemetryRum,
) : OpenTelemetryRum {
    override fun getOpenTelemetry(): OpenTelemetry = delegate.openTelemetry

    override fun getRumSessionId(): String = delegate.rumSessionId

    class Builder constructor(
        private val application: Application,
    ) {
        private val additionalInstrumentations: MutableList<AndroidInstrumentation> =
            mutableListOf(
                ActivityLoadInstrumentation(),
            )
        private var diskBufferingConfig: DiskBufferingConfig =
            DiskBufferingConfig(
                enabled = true,
                maxCacheSize = 10_000_000,
            )
        private var awsRumAppMonitorConfig: AwsRumAppMonitorConfig? = null
        private var applicationName: String? = null
        private var applicationVersion: String? = null
        private val spanExporterCustomizers: MutableList<(SpanExporter) -> SpanExporter> = mutableListOf()
        private val logRecordExporterCustomizers: MutableList<(LogRecordExporter) -> LogRecordExporter> = mutableListOf()
        private var sessionInactivityTimeout: Duration = Duration.ofMinutes(5)
        private var tracerSampler: Sampler? = null

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
         * Configure an application version to send with your spans and logs
         */
        fun setApplicationVersion(config: String): Builder {
            applicationVersion = config
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

        fun addInstrumentation(instrumentation: AndroidInstrumentation): Builder {
            additionalInstrumentations.add(instrumentation)
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

        fun build(): OpenTelemetryAgent {
            if (awsRumAppMonitorConfig == null) {
                throw IllegalStateException("Cannot build OpenTelemetryAgent without an AwsRumAppMonitorConfig")
            }

            val resource = AwsAndroidResource.createDefault(application, awsRumAppMonitorConfig, applicationName)

            val otelRumConfig =
                OtelRumConfig()
                    .setDiskBufferingConfig(diskBufferingConfig)
                    .setSessionTimeout(sessionInactivityTimeout)

            val delegateBuilder =
                OpenTelemetryRumBuilder
                    .create(application, otelRumConfig)
                    .setResource(resource)

            spanExporterCustomizers.forEach { customizer ->
                delegateBuilder.addSpanExporterCustomizer(customizer)
            }
            logRecordExporterCustomizers.forEach { customizer ->
                delegateBuilder.addLogRecordExporterCustomizer(customizer)
            }

            additionalInstrumentations.forEach { instrumentation ->
                delegateBuilder.addInstrumentation(instrumentation)
            }

            if (tracerSampler != null) {
                delegateBuilder.addTracerProviderCustomizer { tracerProviderBuilder, _ ->
                    tracerSampler?.let { tracerProviderBuilder.setSampler(it) }
                }
            }

            return OpenTelemetryAgent(delegateBuilder.build())
        }
    }
}
