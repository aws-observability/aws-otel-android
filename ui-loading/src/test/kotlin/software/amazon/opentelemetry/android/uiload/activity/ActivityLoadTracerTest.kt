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
package software.amazon.opentelemetry.android.uiload.activity

import android.app.Activity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ActivityLoadTracerTest {
    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var tracer: Tracer

    @MockK
    private lateinit var spanBuilder: SpanBuilder

    @MockK
    private lateinit var span: Span

    private lateinit var activityLoadTracer: ActivityLoadTracer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { tracer.spanBuilder(any()) } returns spanBuilder
        every { spanBuilder.setAttribute(any<AttributeKey<String>>(), any()) } returns spanBuilder
        every { spanBuilder.startSpan() } returns span
        every { span.setAttribute(any<AttributeKey<String>>(), any()) } returns span
        every { span.end() } just runs

        activityLoadTracer = ActivityLoadTracer(tracer)
    }

    @Test
    fun `test startSpan creates new span with correct attributes`() {
        val resultSpan = activityLoadTracer.startSpan(activity, "TestSpan")

        verify {
            tracer.spanBuilder("TestSpan")
            spanBuilder.setAttribute(AttributeKey.stringKey("activity.name"), "Activity")
            span.setAttribute(RumConstants.SCREEN_NAME_KEY, any())
        }
        assertEquals(span, resultSpan)
    }

    @Test
    fun `test startSpan returns existing span if already exists`() {
        val firstSpan = activityLoadTracer.startSpan(activity, "TestSpan")
        val secondSpan = activityLoadTracer.startSpan(activity, "TestSpan")

        assertEquals(firstSpan, secondSpan)
        verify(exactly = 1) { spanBuilder.startSpan() }
    }

    @Test
    fun `test endSpan ends and clears span`() {
        activityLoadTracer.startSpan(activity, "TestSpan")
        activityLoadTracer.endSpan(activity)

        verify { span.end() }

        val newSpan = activityLoadTracer.startSpan(activity, "TestSpan")
        verify(exactly = 2) { spanBuilder.startSpan() }
    }

    @Test
    fun `test endSpan handles null`() {
        activityLoadTracer.endSpan(activity)
        verify(exactly = 0) { span.end() }
    }

    @Test
    fun `test multiple activities have separate spans`() {
        val activity2 = mockk<Activity>()
        val span2 = mockk<Span>()

        every { spanBuilder.startSpan() } returnsMany listOf(span, span2)
        every { span2.setAttribute(any<AttributeKey<String>>(), any()) } returns span2

        val span1Result = activityLoadTracer.startSpan(activity, "TestSpan")
        val span2Result = activityLoadTracer.startSpan(activity2, "TestSpan")

        assertNotEquals(span1Result, span2Result)
    }
}
