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

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.nio.charset.StandardCharsets

@Serializable
internal data class AwsConfig(
    @Required val region: String,
    @Required val rumAppMonitorId: String,
    val rumAlias: String? = null,
)

@Serializable
internal data class ExportOverrideConfig(
    val traces: String? = null,
    val logs: String? = null,
    val compression: String = "none",
)

@Serializable
internal data class HttpTelemetryOption(
    val enabled: Boolean,
    val capturedRequestHeaders: List<String>? = null,
    val capturedResponseHeaders: List<String>? = null,
)

@Serializable
internal data class TelemetryConfigs(
    val activity: TelemetryOption? = null,
    val anr: TelemetryOption? = null,
    val crash: TelemetryOption? = null,
    val fragment: TelemetryOption? = null,
    val network: TelemetryOption? = null,
    val slowRendering: TelemetryOption? = null,
    val startup: TelemetryOption? = null,
    val http: HttpTelemetryOption? = null,
    val uiLoad: TelemetryOption? = null,
    val sessionEvents: TelemetryOption? = null,
)

@Serializable
internal data class AgentConfig(
    @Required val aws: AwsConfig,
    val version: String = "1.0.0",
    val exportOverride: ExportOverrideConfig? = null,
    val telemetry: TelemetryConfigs? = null,
    val sessionTimeout: Int = 300,
    val sessionSampleRate: Double = 1.0,
    val applicationAttributes: Map<String, JsonPrimitive>? = null,
    val applicationVersion: String? = null,
)

@Serializable
data class TelemetryOption(
    val enabled: Boolean,
)

@SuppressLint("DiscouragedApi") // Necessary for library modules
internal object AwsConfigReader {
    private const val CONFIG_STRING_KEY = "aws_config"
    val TAG = "AWS Otel Android"

    @OptIn(ExperimentalSerializationApi::class)
    fun readConfig(context: Context): AgentConfig? {
        try {
            val rawResourceId: Int =
                context
                    .getResources()
                    .getIdentifier(CONFIG_STRING_KEY, "raw", context.getPackageName())

            if (rawResourceId == 0) {
                Log.w(TAG, "Config file not found")
                return null
            }
            return context.resources.openRawResource(rawResourceId).use { inputStream ->
                val jsonConfig = String(inputStream.readBytes(), StandardCharsets.UTF_8)
                Json.decodeFromString<AgentConfig>(jsonConfig)
            }
        } catch (e: MissingFieldException) {
            Log.e(TAG, "Missing fields in config: ${e.missingFields}")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read plugin configuration")
            return null
        }
    }

    internal fun buildRumEndpoint(region: String): String = "https://dataplane.rum.$region.amazonaws.com/v1/rum"

    fun getTracesEndpoint(config: AgentConfig): String = config.exportOverride?.traces ?: buildRumEndpoint(config.aws.region)

    fun getLogsEndpoint(config: AgentConfig): String = config.exportOverride?.logs ?: buildRumEndpoint(config.aws.region)
}
