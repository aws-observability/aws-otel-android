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
class CustomAttributesContractTest {
    companion object {
        // pulled from agent-demo aws_config.json:
        val EXPECTED_CUSTOM_ATTRIBUTES =
            mapOf(
                "application.version" to "1.0.0",
                "demo" to "true",
            )
        const val APP_NAME = "Agent Demo Application"
    }

    @Test
    fun `Should have the expected custom attributes in all Agent Demo Application spans and logs`(data: ParsedOtlpData) {
        val appSpans = data.traces.spans().filter { it.attributes.has("service.name", APP_NAME) }
        val appLogs = data.logs.logRecords().filter { it.attributes.has("service.name", APP_NAME) }

        Assertions.assertTrue(
            appSpans.all { span -> span.attributes.has(EXPECTED_CUSTOM_ATTRIBUTES) },
        )
        Assertions.assertTrue(
            appLogs.all { span -> span.attributes.has(EXPECTED_CUSTOM_ATTRIBUTES) },
        )
    }
}
