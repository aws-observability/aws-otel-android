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
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.TelemetryConfig
import java.time.Duration

internal class RumAgentProvider : ContentProvider() {
    companion object {
        const val DEFAULT_COMPRESSION = "none"
    }

    /**
     * Run the ContentProvider onCreate hook, which should begin before the application start
     */
    override fun onCreate(): Boolean {
        if (context != null) {
            try {
                val application = context!!.applicationContext as Application
                val config = AwsConfigReader.readConfig(context!!)
                if (config != null) {
                    initialize(config, application)
                    return true
                }
                Log.e(AwsConfigReader.TAG, "No config file found; cannot initialize AgentProvider")
                return false
            } catch (e: Exception) {
                Log.e(AwsConfigReader.TAG, "Unable to initialize AgentProvider", e)
                return false
            }
        }
        Log.e(AwsConfigReader.TAG, "Context is not available to ContentProvider; cannot initialize AgentProvider")
        return false
    }

    /**
     * Initialize the ADOT Android Client (OpenTelemetryRumClient)
     */
    fun initialize(
        config: AgentConfig,
        application: Application,
    ) {
        OpenTelemetryRumClient {
            awsRum {
                region = config.aws.region
                appMonitorId = config.aws.rumAppMonitorId
                alias = config.aws.rumAlias
            }
            androidApplication = application
            sessionInactivityTimeout = Duration.ofSeconds(config.sessionTimeout.toLong())
            sessionSampleRate = config.sessionSampleRate
            spanExporter =
                OtlpHttpSpanExporter
                    .builder()
                    .setEndpoint(AwsConfigReader.getTracesEndpoint(config))
                    .setCompression(config.exportOverride?.compression ?: DEFAULT_COMPRESSION)
                    .build()
            logRecordExporter =
                OtlpHttpLogRecordExporter
                    .builder()
                    .setEndpoint(AwsConfigReader.getLogsEndpoint(config))
                    .setCompression(config.exportOverride?.compression ?: DEFAULT_COMPRESSION)
                    .build()

            val configTelemetry = config.telemetry
            if (configTelemetry != null) {
                val enabledTelemetries =
                    listOfNotNull(
                        TelemetryConfig.ACTIVITY.takeIf { configTelemetry.activity?.enabled == true },
                        TelemetryConfig.ANR.takeIf { configTelemetry.anr?.enabled == true },
                        TelemetryConfig.CRASH.takeIf { configTelemetry.crash?.enabled == true },
                        TelemetryConfig.FRAGMENT.takeIf { configTelemetry.fragment?.enabled == true },
                        TelemetryConfig.NETWORK.takeIf { configTelemetry.network?.enabled == true },
                        TelemetryConfig.SLOW_RENDERING.takeIf { configTelemetry.slowRendering?.enabled == true },
                        TelemetryConfig.STARTUP.takeIf { configTelemetry.startup?.enabled == true },
                        TelemetryConfig.HTTP_URLCONNECTION.takeIf { configTelemetry.http?.enabled == true },
                        TelemetryConfig.OKHTTP_3.takeIf { configTelemetry.http?.enabled == true },
                        TelemetryConfig.UI_LOADING.takeIf { configTelemetry.uiLoad?.enabled == true },
                    )
                telemetry = enabledTelemetries
                if (configTelemetry.http?.enabled == true) {
                    if (configTelemetry.http.capturedResponseHeaders != null) {
                        capturedResponseHeaders = configTelemetry.http.capturedResponseHeaders
                    }
                    if (configTelemetry.http.capturedRequestHeaders != null) {
                        capturedRequestHeaders = configTelemetry.http.capturedRequestHeaders
                    }
                }
            }
            if (config.applicationAttributes != null) {
                applicationAttributes =
                    config.applicationAttributes.entries.associate {
                        it.key to it.value.content
                    }
            }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
