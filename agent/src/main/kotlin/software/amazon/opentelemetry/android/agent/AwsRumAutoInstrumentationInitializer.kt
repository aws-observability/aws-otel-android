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
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import software.amazon.opentelemetry.android.AwsRumAppMonitorConfig
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.TelemetryConfig
import software.amazon.opentelemetry.android.auth.CognitoCachedCredentialsProvider
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporter
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporter
import java.time.Duration

internal class AwsRumAutoInstrumentationInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        // This will be called before Application.onCreate()
        if (context != null) {
            val config = AwsRumAppMonitorConfigReader.readConfig(context!!)

            val application = context!!.applicationContext as Application

            if (config != null) {
                // Default configuration - sends data to AWS RUM
                val awsRumAppMonitorConfig =
                    AwsRumAppMonitorConfig(
                        config.aws.region,
                        config.aws.rumAppMonitorId,
                        config.aws.rumAlias,
                    )

                val builder =
                    OpenTelemetryAgent
                        .Builder(application)
                        .setAppMonitorConfig(awsRumAppMonitorConfig)
                        .setSessionInactivityTimeout(Duration.ofSeconds(config.sessionTimeout.toLong()))

                // Check if Cognito Identity Pool ID is configured and required classes are available
                if (config.aws.cognitoIdentityPoolId != null && isCognitoAuthAvailable()) {
                    Log.i(AwsRumAppMonitorConfigReader.TAG, "Configuring SigV4 authentication with Cognito Identity Pool")
                    configureCognitoAuth(builder, config)
                } else {
                    if (config.aws.cognitoIdentityPoolId != null && !isCognitoAuthAvailable()) {
                        Log.w(
                            AwsRumAppMonitorConfigReader.TAG,
                            "Cognito Identity Pool ID configured but required dependencies not found. Add cognito-auth and kotlin-sdk-auth dependencies. Falling back to default exporters.",
                        )
                    }
                    // Use default OTLP exporters without authentication
                    configureDefaultExporters(builder, config)
                }

                val telemetry = config.telemetry

                if (telemetry != null) {
                    val enabledTelemetries =
                        listOfNotNull(
                            TelemetryConfig.ACTIVITY.takeIf { telemetry.activity?.enabled == true },
                            TelemetryConfig.ANR.takeIf { telemetry.anr?.enabled == true },
                            TelemetryConfig.CRASH.takeIf { telemetry.crash?.enabled == true },
                            TelemetryConfig.FRAGMENT.takeIf { telemetry.fragment?.enabled == true },
                            TelemetryConfig.NETWORK.takeIf { telemetry.network?.enabled == true },
                            TelemetryConfig.SLOW_RENDERING.takeIf { telemetry.slowRendering?.enabled == true },
                            TelemetryConfig.STARTUP.takeIf { telemetry.startup?.enabled == true },
                            TelemetryConfig.HTTP_URLCONNECTION.takeIf {
                                telemetry.httpUrlConnection?.enabled == true
                            },
                            TelemetryConfig.OKHTTP_3.takeIf { telemetry.okHttp3?.enabled == true },
                            TelemetryConfig.UI_LOADING.takeIf { telemetry.uiLoad?.enabled == true },
                            TelemetryConfig.SESSION_EVENTS.takeIf { telemetry.sessionEvents?.enabled == true },
                        )
                    builder.setEnabledTelemetry(enabledTelemetries)
                }

                config.applicationAttributes?.let { attributes ->
                    builder.setCustomApplicationAttributes(
                        attributes.entries.associate {
                            it.key to it.value.content
                        },
                    )
                }

                builder.build()
            }
        } else {
            Log.e(AwsRumAppMonitorConfigReader.TAG, "Context not available")
        }
        return true
    }

    /**
     * Check if the required Cognito auth classes are available at runtime
     */
    private fun isCognitoAuthAvailable(): Boolean =
        try {
            Class.forName("software.amazon.opentelemetry.android.auth.CognitoCachedCredentialsProvider")
            Class.forName("software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporter")
            Class.forName("software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporter")
            Class.forName("aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient")
            true
        } catch (e: ClassNotFoundException) {
            false
        }

    /**
     * Configure the builder to use default OTLP exporters without authentication
     */
    private fun configureDefaultExporters(
        builder: OpenTelemetryAgent.Builder,
        config: AgentConfig,
    ) {
        builder
            .addSpanExporterCustomizer { _ ->
                OtlpHttpSpanExporter
                    .builder()
                    .setEndpoint(AwsRumAppMonitorConfigReader.getTracesEndpoint(config))
                    .build()
            }.addLogRecordExporterCustomizer { _ ->
                OtlpHttpLogRecordExporter
                    .builder()
                    .setEndpoint(AwsRumAppMonitorConfigReader.getLogsEndpoint(config))
                    .build()
            }
    }

    /**
     * Configure the builder to use Cognito authentication with SigV4 exporters
     */
    private fun configureCognitoAuth(
        builder: OpenTelemetryAgent.Builder,
        config: AgentConfig,
    ) {
        try {
            val client =
                CognitoIdentityClient {
                    region = config.aws.region
                }

            val cognitoCredentialsProvider =
                CognitoCachedCredentialsProvider(
                    cognitoPoolId = config.aws.cognitoIdentityPoolId!!,
                    cognitoClient = client,
                )

            builder
                .addSpanExporterCustomizer { _ ->
                    AwsSigV4SpanExporter
                        .builder()
                        .setRegion(config.aws.region)
                        .setEndpoint(AwsRumAppMonitorConfigReader.getTracesEndpoint(config))
                        .setServiceName("rum")
                        .setCredentialsProvider(cognitoCredentialsProvider)
                        .build()
                }.addLogRecordExporterCustomizer { _ ->
                    AwsSigV4LogRecordExporter
                        .builder()
                        .setRegion(config.aws.region)
                        .setEndpoint(AwsRumAppMonitorConfigReader.getLogsEndpoint(config))
                        .setServiceName("rum")
                        .setCredentialsProvider(cognitoCredentialsProvider)
                        .build()
                }
        } catch (e: Exception) {
            Log.e(
                AwsRumAppMonitorConfigReader.TAG,
                "Failed to configure Cognito authentication. Falling back to default exporters. Make sure aws-runtime:cognito-auth and aws-runtime:kotlin-sdk-auth dependencies are included.",
                e,
            )

            // Fallback to default exporters
            configureDefaultExporters(builder, config)
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
