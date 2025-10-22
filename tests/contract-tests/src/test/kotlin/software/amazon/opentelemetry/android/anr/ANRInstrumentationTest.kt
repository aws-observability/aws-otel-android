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
package software.amazon.opentelemetry.android.anr

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OtlpResolver
import software.amazon.opentelemetry.android.ParsedOtlpData
import software.amazon.opentelemetry.android.getValue
import software.amazon.opentelemetry.android.has
import software.amazon.opentelemetry.android.logRecords
import software.amazon.opentelemetry.android.scopeLogs

@ExtendWith(OtlpResolver::class)
class ANRInstrumentationTest {
    companion object {
        const val ANR_SCOPE = "io.opentelemetry.anr"
        const val ANR_EVENT_NAME = "device.anr"
        const val EXCEPTION_STACK_TRACE_ATTR = "exception.stacktrace"
    }

    @Test
    fun `ANR span is created`(data: ParsedOtlpData) {
        val scopeLogs = data.logs.scopeLogs(ANR_SCOPE)
        val logRecords = scopeLogs.logRecords()
        val anrLogs = logRecords.filter { it.eventName == ANR_EVENT_NAME }

        Assertions.assertTrue(anrLogs.isNotEmpty(), "Logs collection should not be empty")

        Assertions.assertTrue(
            anrLogs.all { log ->
                log.attributes.has(EXCEPTION_STACK_TRACE_ATTR)
            },
        )

        Assertions.assertTrue(
            anrLogs.any { log ->
                log.attributes
                    .getValue(EXCEPTION_STACK_TRACE_ATTR)
                    .stringValue!!
                    .contains("onFinish")
            },
        )
    }
}
