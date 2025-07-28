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
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import io.opentelemetry.android.agent.session.SessionStorage
import io.opentelemetry.android.session.Session
import io.opentelemetry.android.session.SessionObserver
import io.opentelemetry.sdk.common.Clock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.generator.UniqueIdGenerator
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class SessionManagerTest {
    @MockK
    private lateinit var clock: Clock

    @MockK
    private lateinit var sessionIdTimeoutHandler: SessionIdTimeoutHandler

    @MockK
    private lateinit var sessionStorage: SessionStorage

    @MockK
    private lateinit var sessionObserver: SessionObserver

    private lateinit var sessionManager: SessionManager
    private val maxSessionLifetime = Duration.ofHours(4)
    private val initialTimestamp = Instant.now().nano.toLong()
    private val sessionSlot = slot<Session>()

    @BeforeEach
    fun setup() {
        every { clock.now() } returns initialTimestamp
        every { sessionIdTimeoutHandler.hasTimedOut() } returns false
        every { sessionIdTimeoutHandler.refresh() } returns Unit
        every { sessionStorage.save(capture(sessionSlot)) } returns Unit

        // Configure the sessionObserver mock to handle all possible method calls
        every { sessionObserver.onSessionEnded(any()) } returns Unit
        every { sessionObserver.onSessionStarted(any(), any()) } returns Unit

        mockkObject(UniqueIdGenerator)
        every { UniqueIdGenerator.generateId() } returns "test-session-id"

        sessionManager =
            SessionManager(
                clock,
                sessionIdTimeoutHandler,
                sessionStorage,
                maxSessionLifetime,
            )
    }

    @Test
    fun `test initial session is NONE`() {
        // Verify the initial session is saved to storage
        verify { sessionStorage.save(Session.NONE) }
    }

    @Test
    fun `test getSessionId creates new session on first call`() {
        val sessionId = sessionManager.getSessionId()

        println(maxSessionLifetime.toNanos())

        assertEquals("test-session-id", sessionId)
        verify { sessionStorage.save(any()) }
        verify { sessionIdTimeoutHandler.refresh() }
    }

    @Test
    fun `test getSessionId returns same id for subsequent calls`() {
        val firstSessionId = sessionManager.getSessionId()
        val secondSessionId = sessionManager.getSessionId()

        assertEquals(firstSessionId, secondSessionId)
        verify(exactly = 2) { sessionIdTimeoutHandler.refresh() }
    }

    @Test
    fun `test session expires after max lifetime`() {
        // Get initial session ID
        val initialSessionId = sessionManager.getSessionId()

        // Move clock beyond max session lifetime
        every { clock.now() } returns initialTimestamp + maxSessionLifetime.toNanos() + 1000L

        // Generate a new ID for the next session
        every { UniqueIdGenerator.generateId() } returns "new-session-id"

        val newSessionId = sessionManager.getSessionId()

        assertNotEquals(initialSessionId, newSessionId)
        assertEquals("new-session-id", newSessionId)
    }

    @Test
    fun `test session times out based on timeout handler`() {
        // Get initial session ID
        val initialSessionId = sessionManager.getSessionId()

        // Set timeout handler to indicate timeout
        every { sessionIdTimeoutHandler.hasTimedOut() } returns true

        // Generate a new ID for the next session
        every { UniqueIdGenerator.generateId() } returns "timeout-session-id"

        val newSessionId = sessionManager.getSessionId()

        assertNotEquals(initialSessionId, newSessionId)
        assertEquals("timeout-session-id", newSessionId)
    }

    @Test
    fun `test observers are notified of session changes`() {
        // Add observer
        sessionManager.addObserver(sessionObserver)

        // Get initial session ID (creates first session)
        sessionManager.getSessionId()

        // Reset verification counts after initial setup
        io.mockk.clearMocks(sessionObserver, answers = false)

        // Set timeout handler to indicate timeout to force new session
        every { sessionIdTimeoutHandler.hasTimedOut() } returns true
        every { UniqueIdGenerator.generateId() } returns "new-session-id"

        // This should create a new session and notify observers
        sessionManager.getSessionId()

        // Verify observers were notified
        verify(exactly = 1) { sessionObserver.onSessionEnded(any()) }
        verify(exactly = 1) { sessionObserver.onSessionStarted(any(), any()) }
    }
}
