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
package software.amazon.opentelemetry.android.auth.kotlin

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AwsRequestSignerTest : AuthTestBase() {
    @Test
    fun `getSignedHeaders with minimal valid input returns expected headers`() {
        val headers =
            runBlocking {
                AwsRequestSigner.getSignedHeaders(
                    input = "test".toByteArray(),
                    credentialsProvider = credentialsProvider,
                    serviceName = "rum",
                    endpoint = "dataplane.rum.us-east-1.amazonaws.com",
                    region = "us-east-1",
                )
            }

        Assertions.assertNotNull(headers["x-amz-date"])
        Assertions.assertNotNull(headers["authorization"])
        Assertions.assertEquals("application/x-protobuf", headers["content-type"])
    }

    @Test
    fun `getSignedHeaders with failed credentials resolution throws exception`() {
        assertThrows<IllegalStateException> {
            runBlocking {
                AwsRequestSigner.getSignedHeaders(
                    input = "test".toByteArray(),
                    credentialsProvider = failingCredentialsProvider,
                    serviceName = "rum",
                    endpoint = "dataplane.rum.us-east-1.amazonaws.com",
                    region = "us-east-1",
                )
            }
        }
    }
}
