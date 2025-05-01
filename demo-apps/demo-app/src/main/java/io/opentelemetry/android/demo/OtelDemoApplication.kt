/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.android.demo

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.OpenTelemetryRumBuilder
import io.opentelemetry.android.config.OtelRumConfig
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig
import io.opentelemetry.android.instrumentation.sessions.SessionInstrumentation
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import software.amazon.opentelemetry.android.AwsRumAppMonitorConfig
import software.amazon.opentelemetry.android.OpenTelemetryAgent

const val TAG = "otel.demo"

class OtelDemoApplication : Application() {
    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Initializing the opentelemetry-android-agent")

        // 10.0.2.2 is apparently a special binding to the host running the emulator
        val spansIngestUrl = "http://10.0.2.2:4318/v1/traces"
        val logsIngestUrl = "http://10.0.2.2:4318/v1/logs"

        openTelemetryAgent = OpenTelemetryAgent.Builder(this)
            .addSpanExporterCustomizer {
                OtlpHttpSpanExporter.builder()
                    .setEndpoint(spansIngestUrl)
                    .build()
            }
            .addLogRecordExporterCustomizer {
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint(logsIngestUrl)
                    .build()
            }
            .setApplicationName("demo.app")
            .setAppMonitorConfig(
                AwsRumAppMonitorConfig(appMonitorId = "test-app-monitor", region = "us-east-1")
            )
            .setApplicationVersion("1.0.0")
            .build()

        try {
            Log.d(TAG, "RUM session started: " + openTelemetryAgent?.rumSessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Oh no!", e)
        }

    }

    // This is not used but it's needed to verify that our consumer proguard rules cover this use case.
    private fun initializeOtelWithGrpc() {

        val builder = OpenTelemetryAgent.Builder(this)
            .addSpanExporterCustomizer {
                OtlpGrpcSpanExporter.builder().build()
            }
            .addLogRecordExporterCustomizer {
                OtlpGrpcLogRecordExporter.builder().build()
            }
            .setApplicationName("demo.app")
            .setAppMonitorConfig(
                AwsRumAppMonitorConfig(appMonitorId = "test-app-monitor", region = "us-east-1")
            )
            .setApplicationVersion("1.0.0")
            .build()

        // This is an overly-cautious measure to prevent R8 from discarding away the whole method
        // in case it identifies that it's actually not doing anything meaningful.
        if (System.currentTimeMillis() < 0) {
            print(builder)
        }
    }

    companion object {
        var openTelemetryAgent: OpenTelemetryAgent? = null

        fun tracer(name: String): Tracer? {
            return  openTelemetryAgent?.openTelemetry?.tracerProvider?.get(name)
        }

        fun counter(name: String): LongCounter? {
            return  openTelemetryAgent?.openTelemetry?.meterProvider?.get("demo.app")?.counterBuilder(name)?.build()
        }

        fun eventBuilder(scopeName: String, eventName: String): LogRecordBuilder {
            val logger = openTelemetryAgent?.openTelemetry?.logsBridge?.loggerBuilder(scopeName)?.build()
            var builder: ExtendedLogRecordBuilder = logger?.logRecordBuilder() as ExtendedLogRecordBuilder
            return builder.setEventName(eventName)
        }
    }
}
