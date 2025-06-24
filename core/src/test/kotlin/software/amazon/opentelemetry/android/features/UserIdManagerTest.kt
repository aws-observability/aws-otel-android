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
package software.amazon.opentelemetry.android.features

import android.app.Application
import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.opentelemetry.api.trace.TraceId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserIdManagerTest {
    @MockK
    private lateinit var diskManager: DiskManager

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var context: Context

    @BeforeEach
    fun setup() {
        every { diskManager.writeToFile(context, any(), any()) } returns true
    }

    @Test
    fun `test install with no existing user id`() {
        val userIdManager = UserIdManager(diskManager)
        every { diskManager.readFromFileIfExists(context, any()) } returns null

        mockkStatic(TraceId::class)
        every { TraceId.fromLongs(any(), any()) } returns "my-random-id"

        userIdManager.install(application, context)

        Assertions.assertTrue(userIdManager.userId.isNotBlank())
        Assertions.assertEquals("my-random-id", userIdManager.userId)
        Assertions.assertEquals(
            mapOf(
                UserIdManager.USER_ID_ATTR to "my-random-id",
            ),
            userIdManager.buildAttributes(),
        )
    }

    @Test
    fun `test install with existing user id in disk`() {
        val userIdManager = UserIdManager(diskManager)
        every { diskManager.readFromFileIfExists(context, any()) } returns "my-existing-id"

        userIdManager.install(application, context)

        Assertions.assertEquals("my-existing-id", userIdManager.userId)
        Assertions.assertEquals(
            mapOf(
                UserIdManager.USER_ID_ATTR to "my-existing-id",
            ),
            userIdManager.buildAttributes(),
        )
    }
}
