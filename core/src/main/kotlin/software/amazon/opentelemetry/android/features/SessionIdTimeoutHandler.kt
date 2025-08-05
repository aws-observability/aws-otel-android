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

import io.opentelemetry.sdk.common.Clock
import java.time.Duration

/**
 * This class takes in [sessionInactivityTimeout] and an OpenTelemetry SDK [clock] and will manage
 * session state and timeout for use in the SessionManager class.
 *
 * Some notes:
 *  - An application that is in the *foreground* is assumed to be always active.
 *  - If an application is foregrounded after being in the background, we may expire the session
 *      if enough time has passed, hence the TRANSITIONING_TO_FOREGROUND state
 */
class SessionIdTimeoutHandler(
    private val sessionInactivityTimeout: Duration,
    private val clock: Clock = Clock.getDefault(),
) {
    private var timeoutStartNanos: Long = 0
    private var state = State.FOREGROUND

    fun onApplicationForegrounded() {
        state = State.TRANSITIONING_TO_FOREGROUND
    }

    fun onApplicationBackgrounded() {
        state = State.BACKGROUND
    }

    fun hasTimedOut(): Boolean {
        if (state == State.FOREGROUND) {
            return false
        }
        val elapsedTime = clock.nanoTime() - timeoutStartNanos
        return elapsedTime >= sessionInactivityTimeout.toNanos()
    }

    fun refresh() {
        timeoutStartNanos = clock.nanoTime()
        if (state == State.TRANSITIONING_TO_FOREGROUND) {
            state = State.FOREGROUND
        }
    }

    fun getTimeoutStartNanos(): Long = timeoutStartNanos

    private enum class State {
        FOREGROUND,
        BACKGROUND,
        TRANSITIONING_TO_FOREGROUND,
    }
}
