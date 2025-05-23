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

import org.awaitility.Awaitility.await
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.TraceRoot
import software.amazon.opentelemetry.android.otlp.parser.OtlpFileParser
import java.io.File
import java.util.concurrent.TimeUnit

data class ParsedOtlpData(
    val logs: List<LogRoot>,
    val traces: List<TraceRoot>,
)

class OtlpResolver : ParameterResolver {
    companion object {
        const val LOGS_LOCATION = "/tmp/otel-android-collector/logs.txt"
        const val TRACES_LOCATION = "/tmp/otel-android-collector/traces.txt"

        var logData: List<LogRoot>? = null
        var traceData: List<TraceRoot>? = null
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type == ParsedOtlpData::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any {
        parseData()
        return ParsedOtlpData(logData!!, traceData!!)
    }

    private fun parseData() {
        synchronized(this) {
            if (!File(LOGS_LOCATION).exists() || !File(TRACES_LOCATION).exists()) {
                await()
                    .atMost(20, TimeUnit.SECONDS)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .until {
                        File(LOGS_LOCATION).exists() && File(TRACES_LOCATION).exists()
                    }
            }

            if (logData == null) {
                logData = OtlpFileParser.readLogsFile(File(LOGS_LOCATION))
            }
            if (traceData == null) {
                traceData = OtlpFileParser.readTracesFile(File(TRACES_LOCATION))
            }
        }
    }
}
