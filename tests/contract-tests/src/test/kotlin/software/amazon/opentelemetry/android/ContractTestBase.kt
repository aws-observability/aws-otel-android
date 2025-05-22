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

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.TraceRoot
import software.amazon.opentelemetry.android.otlp.parser.OtlpFileParser
import java.io.File

@ExtendWith(MockitoExtension::class)
abstract class ContractTestBase {
    companion object {
        val projectRoot1 = System.getProperty("user.dir")

        // Arrange
        val outDir = File("$projectRoot1/../out")
        private val logsOutFile = File(outDir, "logs.txt")
        private val tracesOutFile = File(outDir, "traces.txt")

        lateinit var logs: List<LogRoot>
        lateinit var traces: List<TraceRoot>

        @JvmStatic
        @BeforeAll
        fun setUp() {
            logs = OtlpFileParser.readLogsFile(logsOutFile)
            traces = OtlpFileParser.readTracesFile(tracesOutFile)
        }
    }
}
