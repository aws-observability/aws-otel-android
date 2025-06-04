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
class FragmentInstrumentationTest {
    companion object {
        const val FRAGMENT_NAME_ATTR = "fragment.name"
        const val SCREEN_NAME_ATTR = "screen.name"
    }

    @Test
    fun `New fragment lifecycle span is created with all correct span events`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans("io.opentelemetry.lifecycle")
        val spans = scopeSpans.spans("Created")

        Assertions.assertTrue(
            spans.any {
                it.attributes.has(FRAGMENT_NAME_ATTR, "InstrumentationTestFragment")
            },
        )
        Assertions.assertTrue(
            spans.any {
                it.attributes.has(SCREEN_NAME_ATTR, "InstrumentationTestFragment")
            },
        )
    }
}
