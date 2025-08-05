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

import io.opentelemetry.android.agent.session.SessionStorage
import io.opentelemetry.android.session.Session
import io.opentelemetry.android.session.SessionObserver
import io.opentelemetry.android.session.SessionProvider
import io.opentelemetry.android.session.SessionPublisher
import io.opentelemetry.sdk.common.Clock
import software.amazon.opentelemetry.android.generator.UniqueIdGenerator
import java.time.Duration

/**
 * This class extends the OpenTelemetry Android SessionProvider interface to provide us logic
 * for managing sessions and attaching observers for session event instrumentation.
 *
 * This should look very similar to the upstream SessionManager implementation. Theirs, however, is
 * internal. Thus our need to maintain our own version of this component
 *
 * We define a session as having expired when one of the two conditions pass:
 *  1. More than [maxSessionLifetime] has passed (default of 4 hours)
 *  2. The session has been 'inactive' (not generating telemetry) for some period of time (default of 5 minutes)
 */
class SessionManager(
    private val clock: Clock = Clock.getDefault(),
    private val sessionIdTimeoutHandler: SessionIdTimeoutHandler,
    private val sessionStorage: SessionStorage = SessionStorage.InMemory(),
    private val maxSessionLifetime: Duration = Duration.ofHours(4),
    private val bufferedObservedSessionsMaxSize: Int = 128,
) : SessionProvider,
    SessionPublisher {
    private var session: Session = Session.NONE
    private val observers: MutableList<SessionObserver> = mutableListOf()

    // when observers get added after the SessionManager has reported its first session, observers
    // can miss events. hence, we add a buffer to hold a max of N sessions to help ensure observers
    // don't miss this data
    private val bufferedObservedSessions: MutableList<Pair<Session, Session>> = mutableListOf()

    init {
        sessionStorage.save(session)
    }

    override fun addObserver(observer: SessionObserver) {
        addObserver(observer, true)
    }

    fun addObserver(
        observer: SessionObserver,
        observeFromBuffer: Boolean,
    ) {
        synchronized(this) {
            observers.add(observer)
            if (observeFromBuffer) {
                bufferedObservedSessions.forEach { observedSession ->
                    val (prevSession, newSession) = observedSession
                    observer.onSessionEnded(prevSession)
                    observer.onSessionStarted(newSession, prevSession)
                }
            }
        }
    }

    override fun getSessionId(): String {
        var newSession = session
        val sessionExpired = sessionHasExpired()
        val sessionTimedOut = sessionIdTimeoutHandler.hasTimedOut()

        if (sessionExpired || sessionTimedOut) {
            val newId = UniqueIdGenerator.generateId()
            newSession = Session.DefaultSession(newId, clock.now())
            sessionStorage.save(newSession)
        }

        sessionIdTimeoutHandler.refresh()

        if (newSession != session) {
            val prevSession = session
            session = newSession

            // Add buffered sessions
            bufferedObservedSessions.add(
                Pair(
                    Session.DefaultSession(prevSession.getId(), prevSession.getStartTimestamp()),
                    Session.DefaultSession(newSession.getId(), newSession.getStartTimestamp()),
                ),
            )

            // Evict the first session from buffer when it becomes full
            if (bufferedObservedSessions.size > bufferedObservedSessionsMaxSize) {
                bufferedObservedSessions.removeFirst()
            }

            observers.forEach {
                it.onSessionEnded(prevSession)
                it.onSessionStarted(session, prevSession)
            }
        }

        return session.getId()
    }

    private fun sessionHasExpired(): Boolean {
        if (session == Session.NONE) {
            return true
        }
        val elapsedTime = clock.now() - session.getStartTimestamp()
        return elapsedTime >= maxSessionLifetime.toNanos()
    }

    fun getBufferedObservedSessions(): List<Pair<Session, Session>> = bufferedObservedSessions
}
