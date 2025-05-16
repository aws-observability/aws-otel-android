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
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Serializable
internal data class ApplicationConfig(
    @Required val applicationVersion: String,
)

@Serializable
internal data class EndpointConfig(
    val spans: String? = null,
    val logs: String? = null,
)

@Serializable
internal data class RumConfig(
    @Required val region: String,
    @Required val appMonitorId: String,
    val overrideEndpoint: EndpointConfig? = null,
)

@Serializable
internal data class ConfigFile(
    @Contextual @Required val rum: RumConfig,
    @Required val application: ApplicationConfig,
)

@SuppressLint("DiscouragedApi") // Necessary for library modules
internal object AwsRumAppMonitorConfigReader {
    private const val CONFIG_STRING_KEY = "aws_config"
    val TAG = "AWS Otel Android"

    @OptIn(ExperimentalSerializationApi::class)
    fun readConfig(context: Context): ConfigFile? {
        try {
            val rawResourceId: Int =
                context
                    .getResources()
                    .getIdentifier(CONFIG_STRING_KEY, "raw", context.getPackageName())

            if (rawResourceId == 0) {
                Log.w(TAG, "Config file not found")
                return null
            }
            val inputStream: InputStream = context.getResources().openRawResource(rawResourceId)
            val jsonConfig = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            return Json.decodeFromString<ConfigFile>(jsonConfig)
        } catch (e: MissingFieldException) {
            Log.e(TAG, "Missing fields in config: ${e.missingFields}")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read plugin configuration")
            return null
        }
    }

    internal fun buildRumEndpoint(region: String): String = "https://dataplane.rum.$region.amazonaws.com/v1/rum"

    fun getSpansEndpoint(config: ConfigFile): String = config.rum.overrideEndpoint?.spans ?: buildRumEndpoint(config.rum.region)

    fun getLogsEndpoint(config: ConfigFile): String = config.rum.overrideEndpoint?.logs ?: buildRumEndpoint(config.rum.region)
}
