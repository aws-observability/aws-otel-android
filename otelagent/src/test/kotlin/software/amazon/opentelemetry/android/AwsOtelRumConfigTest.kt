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

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class AwsOtelRumConfigTest {
    @Test
    fun `should Create Correct Config With AWS Region And RumId`() {
        val config =
            AwsOtelRumConfig()
                .setGlobalAttributes(
                    Attributes.of(
                        AttributeKey.stringKey("toolkit"),
                        "jetpack compose",
                    ),
                ).setAwsRegion("test-region")
                .setRumAppMonitor("test-rum-id")

        Assertions.assertEquals("test-region", config.awsRegion)
        Assertions.assertEquals("test-rum-id", config.rumAppMonitor)
    }
}
