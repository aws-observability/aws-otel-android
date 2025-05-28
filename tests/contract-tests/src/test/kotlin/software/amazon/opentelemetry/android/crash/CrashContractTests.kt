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
package software.amazon.opentelemetry.android.crash

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OtlpResolver
import software.amazon.opentelemetry.android.ParsedOtlpData
import software.amazon.opentelemetry.android.attributes
import software.amazon.opentelemetry.android.logRecords
import software.amazon.opentelemetry.android.scopeLogs

@ExtendWith(OtlpResolver::class)
class CrashContractTests {
    companion object {
        val STACK_TRACE_EXCEPTION_ATTRIBUTE = "exception.stacktrace"
        val EVENT_NAME_ATTRIBUTE = "event.name"
        val CRASH_SCOPE = "io.opentelemetry.crash"
        val EXCEPTION_MESSAGE_ATTR = "exception.message"
        val EXCEPTION_TYPE_ATTR = "exception.type"
        val THREAD_ID_ATTR = "thread.id"
        val THREAD_NAME_ATTR = "thread.name"
    }

    @Test
    fun `logs must be generated for device crash`(data: ParsedOtlpData) {
        val scopeLogs = data.logs.scopeLogs(CRASH_SCOPE)

        val logRecords = scopeLogs.logRecords()

        Assertions.assertEquals(
            logRecords.attributes(EVENT_NAME_ATTRIBUTE).value.stringValue,
            "device.crash",
        )

        Assertions.assertEquals(
            logRecords.attributes(EXCEPTION_MESSAGE_ATTR).value.stringValue,
            "java.lang.reflect.InvocationTargetException",
        )
        Assertions.assertEquals(
            logRecords.attributes(EXCEPTION_TYPE_ATTR).value.stringValue,
            "java.lang.RuntimeException",
        )

        Assertions.assertEquals(
            logRecords.attributes(THREAD_NAME_ATTR).value.stringValue,
            "main",
        )

        Assertions.assertEquals(
            logRecords.attributes(THREAD_ID_ATTR).value.intValue,
            "2",
        )

        Assertions.assertNotNull(logRecords.attributes(STACK_TRACE_EXCEPTION_ATTRIBUTE).value.stringValue)

        Assertions.assertTrue(
            logRecords
                .attributes(STACK_TRACE_EXCEPTION_ATTRIBUTE)
                .value.stringValue!!
                .contains("Testing exception"),
        )
    }
}
