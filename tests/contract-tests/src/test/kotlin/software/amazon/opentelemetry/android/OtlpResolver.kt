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

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.TraceRoot
import software.amazon.opentelemetry.android.otlp.parser.OtlpFileParser
import java.io.File

data class ParsedOtlpData(
    val logs: List<LogRoot>,
    val traces: List<TraceRoot>,
)

class OtlpResolver : ParameterResolver {
    companion object {
        const val LOGS_LOCATION = "/tmp/logs.txt"
        const val TRACES_LOCATION = "/tmp/traces.txt"
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type == ParsedOtlpData::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any {
        val logs: List<LogRoot> = OtlpFileParser.readLogsFile(File(LOGS_LOCATION))
        val traces: List<TraceRoot> = OtlpFileParser.readTracesFile(File(TRACES_LOCATION))
        return ParsedOtlpData(logs, traces)
    }
}
