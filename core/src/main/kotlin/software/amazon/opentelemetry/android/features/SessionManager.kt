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

class SessionManager(
    private val clock: Clock = Clock.getDefault(),
    private val sessionIdTimeoutHandler: SessionIdTimeoutHandler,
    private val sessionStorage: SessionStorage = SessionStorage.InMemory(),
    private val maxSessionLifetime: Duration = Duration.ofHours(4),
) : SessionProvider,
    SessionPublisher {
    private var session: Session = Session.NONE
    private val observers: MutableList<SessionObserver> = mutableListOf()

    private val preObservedSessions: MutableList<Pair<Session, Session>> = mutableListOf()

    init {
        sessionStorage.save(session)
    }

    override fun addObserver(observer: SessionObserver) {
        synchronized(this) {
            observers.add(observer)
            preObservedSessions.forEach { observedSession ->
                val (prevSession, newSession) = observedSession
                observer.onSessionEnded(prevSession)
                observer.onSessionStarted(newSession, prevSession)
            }
            preObservedSessions.clear()
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

            // Add pre-observed sessions if there is no observer
            if (observers.isEmpty()) {
                preObservedSessions.add(
                    Pair(
                        Session.DefaultSession(prevSession.getId(), prevSession.getStartTimestamp()),
                        Session.DefaultSession(newSession.getId(), newSession.getStartTimestamp()),
                    ),
                )
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
            1
            return true
        }
        val elapsedTime = clock.now() - session.getStartTimestamp()
        return elapsedTime >= maxSessionLifetime.toNanos()
    }
}
