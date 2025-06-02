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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OtlpResolver::class)
class SessionContractTest {
    companion object {
        const val SESSION_ID_ATTR = "session.id"
    }

    @Test
    fun `Spans and logs should have a sessionid in attributes`(data: ParsedOtlpData) {
        Assertions.assertTrue(
            data.traces.spans().all { span -> span.attributes.has(SESSION_ID_ATTR) },
        )
        Assertions.assertTrue(
            data.logs.logRecords().all { logRecord ->
                logRecord.attributes.has(SESSION_ID_ATTR)
            },
        )
    }

    @Test
    fun `Spans and logs should have a random session Id generated`(data: ParsedOtlpData) {
        val sessionIdsFromSpans =
            data.traces
                .spans()
                .flatMap { span ->
                    span.attributes
                        .filter { attribute ->
                            attribute.key == SESSION_ID_ATTR
                        }.map { attribute -> attribute.value.stringValue }
                }.toSet()

        val sessionIdsFromLogs =
            data.logs
                .logRecords()
                .flatMap { span ->
                    span.attributes
                        .filter { attribute ->
                            attribute.key == SESSION_ID_ATTR
                        }.map { attribute -> attribute.value.stringValue }
                }.toSet()

        Assertions.assertTrue(
            sessionIdsFromSpans.count() > 1,
        )
        Assertions.assertTrue(
            sessionIdsFromLogs.count() > 1,
        )
    }
}
