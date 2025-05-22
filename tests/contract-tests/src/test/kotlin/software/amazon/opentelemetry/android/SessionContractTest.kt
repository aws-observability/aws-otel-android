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
    @Test
    fun `spans and logs should have a random sessionid in attributes`(data: ParsedOtlpData) {
        Assertions.assertTrue(
            data.traces.resources().attributeKeyExists("session.id"),
        )
        Assertions.assertTrue(
            data.logs.resources().attributeKeyExists("session.id"),
        )
    }
}
