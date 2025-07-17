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

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.opentelemetry.sdk.common.Clock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class SessionIdTimeoutHandlerTest {
    @MockK
    private lateinit var clock: Clock

    private val sessionInactivityTimeout = Duration.ofMinutes(30)

    @BeforeEach
    fun setup() {
        // Start with a base time of 1000
        every { clock.nanoTime() } returns 1000L
    }

    @Test
    fun `test initial state is foreground and not timed out`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)
        assertFalse(sessionIdTimeoutHandler.hasTimedOut())
    }

    @Test
    fun `test refresh updates timeout timestamp`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)

        // Initial refresh
        sessionIdTimeoutHandler.refresh()

        // Move time forward but less than timeout
        every { clock.nanoTime() } returns 1000L + sessionInactivityTimeout.toNanos() / 2

        // Should not time out
        assertFalse(sessionIdTimeoutHandler.hasTimedOut())
    }

    @Test
    fun `test background state with timeout`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)

        // Set to background state
        sessionIdTimeoutHandler.onApplicationBackgrounded()
        sessionIdTimeoutHandler.refresh()

        // Move time forward beyond timeout
        every { clock.nanoTime() } returns 1000L + sessionInactivityTimeout.toNanos() + 1000L

        // Should time out
        assertTrue(sessionIdTimeoutHandler.hasTimedOut())
    }

    @Test
    fun `test background state without timeout`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)

        // Set to background state
        sessionIdTimeoutHandler.onApplicationBackgrounded()
        sessionIdTimeoutHandler.refresh()

        // Move time forward but less than timeout
        every { clock.nanoTime() } returns 1000L + sessionInactivityTimeout.toNanos() / 2

        // Should not time out
        assertFalse(sessionIdTimeoutHandler.hasTimedOut())
    }

    @Test
    fun `test transition to foreground state`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)

        // Set to background state first
        sessionIdTimeoutHandler.onApplicationBackgrounded()

        // Then transition to foreground
        sessionIdTimeoutHandler.onApplicationForegrounded()

        // Should be in transitioning state, which can time out
        every { clock.nanoTime() } returns 1000L + sessionInactivityTimeout.toNanos() + 1000L
        assertTrue(sessionIdTimeoutHandler.hasTimedOut())

        // After refresh, should be in foreground state
        sessionIdTimeoutHandler.refresh()
        assertFalse(sessionIdTimeoutHandler.hasTimedOut())
    }

    @Test
    fun `test foreground state never times out`() {
        val sessionIdTimeoutHandler = SessionIdTimeoutHandler(sessionInactivityTimeout, clock)

        // Ensure we're in foreground state
        sessionIdTimeoutHandler.refresh()

        // Move time forward beyond timeout
        every { clock.nanoTime() } returns 1000L + sessionInactivityTimeout.toNanos() * 2

        // Should not time out in foreground state
        assertFalse(sessionIdTimeoutHandler.hasTimedOut())
    }
}
