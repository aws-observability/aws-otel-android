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
package software.amazon.opentelemetry.android.instrumentation

import android.os.Build
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OtlpResolver
import software.amazon.opentelemetry.android.ParsedOtlpData
import software.amazon.opentelemetry.android.findSpanEvents
import software.amazon.opentelemetry.android.has
import software.amazon.opentelemetry.android.scopeSpans
import software.amazon.opentelemetry.android.spans

@ExtendWith(OtlpResolver::class)
class ActivityContractTest {
    companion object {
        const val SCOPE_NAME = "io.opentelemetry.lifecycle"

        val CREATED_SPAN_EVENTS =
            listOf(
                "activityCreated",
                "activityStarted",
                "activityResumed",
            )
        val CREATED_SPAN_EVENTS_POST_ANDROID_Q =
            listOf(
                "activityPreCreated",
                "activityPostCreated",
                "activityPreStarted",
                "activityPostStarted",
                "activityPreResumed",
                "activityPostResumed",
            )

        val PAUSED_SPAN_EVENTS =
            listOf(
                "activityPaused",
            )

        val PAUSED_SPAN_EVENTS_POST_ANDROID_Q =
            listOf(
                "activityPrePaused",
                "activityPostPaused",
            )

        val RESTARTED_SPAN_EVENTS =
            listOf(
                "activityStarted",
                "activityResumed",
            )

        val RESTARTED_SPAN_EVENTS_POST_ANDROID_Q =
            listOf(
                "activityPreStarted",
                "activityPostStarted",
                "activityPreResumed",
                "activityPostResumed",
            )
        val androidVersion = System.getProperty("androidVersion")!!.toInt()
    }

    @Test
    fun `Expected opentelemetry lifecycle span scope created`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        Assertions.assertFalse(scopeSpans.isEmpty())
        Assertions.assertFalse(scopeSpans.all { it.spans.isEmpty() })
    }

    @Test
    fun `AppStart span created on app startup`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val spans = scopeSpans.spans("AppStart")

        Assertions.assertTrue(
            spans.any {
                it.attributes.has("start.type", "cold")
            },
        )
    }

    @Test
    fun `Created span created for first MainActivity load`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val createdSpan = scopeSpans.spans("Created", mapOf("screen.name" to "MainActivity")).first()
        Assertions.assertNotNull(createdSpan)
        if (androidVersion >= Build.VERSION_CODES.Q) {
            Assertions.assertTrue(
                createdSpan.findSpanEvents(
                    CREATED_SPAN_EVENTS.plus(
                        CREATED_SPAN_EVENTS_POST_ANDROID_Q,
                    ),
                ),
            )
        } else {
            Assertions.assertTrue(createdSpan.findSpanEvents(CREATED_SPAN_EVENTS))
        }
    }

    @Test
    fun `Paused span created for navigating away from MainActivity`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val pausedSpan = scopeSpans.spans("Paused", mapOf("screen.name" to "MainActivity")).first()
        Assertions.assertNotNull(pausedSpan)

        if (androidVersion >= Build.VERSION_CODES.Q) {
            Assertions.assertTrue(
                pausedSpan.findSpanEvents(
                    PAUSED_SPAN_EVENTS.plus(
                        PAUSED_SPAN_EVENTS_POST_ANDROID_Q,
                    ),
                ),
            )
        } else {
            Assertions.assertTrue(pausedSpan.findSpanEvents(PAUSED_SPAN_EVENTS))
        }
    }

    @Test
    fun `Created span created for SecondActivity load`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val createdSpan = scopeSpans.spans("Created", mapOf("screen.name" to "SecondActivity")).first()
        Assertions.assertNotNull(createdSpan)

        if (androidVersion >= Build.VERSION_CODES.Q) {
            Assertions.assertTrue(
                createdSpan.findSpanEvents(
                    CREATED_SPAN_EVENTS.plus(
                        CREATED_SPAN_EVENTS_POST_ANDROID_Q,
                    ),
                ),
            )
        } else {
            Assertions.assertTrue(createdSpan.findSpanEvents(CREATED_SPAN_EVENTS))
        }
    }

    @Test
    fun `Restarted span created for MainActivity load`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(SCOPE_NAME)
        val restartedSpan =
            scopeSpans
                .spans(
                    "Restarted",
                    mapOf(
                        "screen.name" to "MainActivity",
                        "last.screen.name" to "InstrumentationTestFragment",
                    ),
                ).first()
        Assertions.assertNotNull(restartedSpan)
        if (androidVersion >= Build.VERSION_CODES.Q) {
            Assertions.assertTrue(
                restartedSpan.findSpanEvents(
                    RESTARTED_SPAN_EVENTS.plus(
                        RESTARTED_SPAN_EVENTS_POST_ANDROID_Q,
                    ),
                ),
            )
        } else {
            Assertions.assertTrue(restartedSpan.findSpanEvents(RESTARTED_SPAN_EVENTS))
        }
    }
}
