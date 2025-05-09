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
package software.amazon.opentelemetry.android.zerocode

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import software.amazon.opentelemetry.android.AwsRumAppMonitorConfig
import java.io.InputStream
import java.nio.charset.StandardCharsets

internal data class ConfigFile(
    val rum: AwsRumAppMonitorConfig,
)

@SuppressLint("DiscouragedApi") // Necessary for library modules
internal object AwsRumAppMonitorConfigReader {
    private const val CONFIG_STRING_KEY = "aws_config"
    private val gson: Gson = Gson()
    private val TAG = "AWS Otel Android"

    fun readConfig(context: Context): ConfigFile? {
        try {
            val rawResourceId: Int =
                context
                    .getResources()
                    .getIdentifier(CONFIG_STRING_KEY, "raw", context.getPackageName())

            if (rawResourceId == 0) {
                Log.d(TAG, "Config file not found")
                return null
            }
            val inputStream: InputStream = context.getResources().openRawResource(rawResourceId)
            val jsonConfig = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            return gson.fromJson(jsonConfig, ConfigFile::class.java)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to read plugin configuration")
            return null
        }
    }
}
