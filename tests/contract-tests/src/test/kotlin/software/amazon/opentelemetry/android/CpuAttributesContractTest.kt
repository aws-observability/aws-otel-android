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
class CpuAttributesContractTest {
    companion object {
        const val APP_NAME = "Agent Demo Application"
    }

    @Test
    fun `Spans with duration should have cpu utilization attributes`(data: ParsedOtlpData) {
        val appSpans =
            data.traces.spans().filter {
                it.attributes.has("service.name", APP_NAME)
            }

        Assertions.assertTrue(
            appSpans.all { span ->
                !span.hasDuration() || span.attributes.has("process.cpu.avg_utilization")
            },
        )
    }
}
