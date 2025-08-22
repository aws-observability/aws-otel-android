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
package software.amazon.opentelemetry.android.instrumentation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OtlpResolver
import software.amazon.opentelemetry.android.ParsedOtlpData
import software.amazon.opentelemetry.android.getValue
import software.amazon.opentelemetry.android.has
import software.amazon.opentelemetry.android.scopeSpans
import software.amazon.opentelemetry.android.spans

@ExtendWith(OtlpResolver::class)
class UILoadContractTest {
    companion object {
        const val SCOPE_NAME = "software.amazon.opentelemetry.ui-loading"
        const val SPAN_NAME = "TimeToFirstDraw"
    }

    @Test
    fun `TimeToFirstDraw spans have all expected attributes`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val spans = scopeSpans.spans(SPAN_NAME)
        Assertions.assertFalse(spans.isEmpty())

        val mainActivityTTFD = spans.filter { it.attributes.has("screen.name", "MainActivity") }
        val secondActivityTTFD = spans.filter { it.attributes.has("screen.name", "SecondActivity") }

        Assertions.assertFalse(mainActivityTTFD.isEmpty())
        Assertions.assertFalse(secondActivityTTFD.isEmpty())

        Assertions.assertTrue(
            mainActivityTTFD.all { span ->
                val nodes =
                    span.attributes
                        .getValue("screen.view.nodes")
                        .intValue!!
                        .toInt()
                val depth =
                    span.attributes
                        .getValue("screen.view.depth")
                        .intValue!!
                        .toInt()

                // these values can vary a lot depending on device / os version, hard to test correctness
                // let's just sanity test
                nodes >= 1 && depth >= 1
            },
        )
        Assertions.assertTrue(
            secondActivityTTFD.all { span ->
                val nodes =
                    span.attributes
                        .getValue("screen.view.nodes")
                        .intValue!!
                        .toInt()
                val depth =
                    span.attributes
                        .getValue("screen.view.depth")
                        .intValue!!
                        .toInt()
                nodes >= 1 && depth >= 1
            },
        )
    }
}
