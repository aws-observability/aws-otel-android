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
package software.amazon.opentelemetry.android.api

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OpenTelemetryAgent

@ExtendWith(MockKExtension::class)
class AwsRumTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setupClass() {
            mockkObject(OpenTelemetryAgent)
            every { OpenTelemetryAgent.getOpenTelemetryAgent() } returns mockk(relaxed = true)
        }
    }

    @Test
    fun `getInstance returns singleton instance`() {
        val instance1 = AwsRum.getInstance()
        val instance2 = AwsRum.getInstance()
        assertSame(instance1, instance2)
    }
}
