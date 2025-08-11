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
import android.os.Bundle
import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.opentelemetry.api.trace.Span
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import software.amazon.opentelemetry.android.uiload.utils.FirstDrawListener

class ActivityLoadCallbackTest {
    @MockK
    private lateinit var tracers: ActivityLoadTracer

    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var firstDrawListener: FirstDrawListener

    private lateinit var activityLoadCallback: ActivityLoadCallback

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        activityLoadCallback = ActivityLoadCallback(tracers, firstDrawListener)
    }

    @Test
    fun `test TimeToFirstDraw span starts when Activity onCreate() is invoked and ends when the callback is invoked`() {
        val bundle = mock<Bundle>(Bundle::class.java)
        val drawCallbackSlot = slot<(View) -> Unit>()
        every { firstDrawListener.registerFirstDraw(activity, capture(drawCallbackSlot)) } just runs
        every { tracers.startSpan(activity, "TimeToFirstDraw") } returns mockk<Span>()
        every { tracers.endSpan(activity, any()) } just runs

        activityLoadCallback.onActivityPreCreated(activity, bundle)
        verify { tracers.startSpan(activity, "TimeToFirstDraw") }

        verify { firstDrawListener.registerFirstDraw(activity, any()) }

        drawCallbackSlot.captured.invoke(mockk<View>())
        verify { tracers.endSpan(activity, any()) }
    }

    @Test
    fun `test TimeToFirstDraw spans tracking for multiple activities`() {
        val activity1 = mockk<Activity>()
        val activity2 = mockk<Activity>()

        every { firstDrawListener.registerFirstDraw(any(), any()) } just runs
        every { tracers.startSpan(any(), any()) } returns mockk<Span>()
        every { tracers.endSpan(any(), any()) } just runs

        activityLoadCallback.onActivityPreCreated(activity1, null)
        activityLoadCallback.onActivityPreCreated(activity2, null)

        verify(exactly = 1) { tracers.startSpan(activity1, "TimeToFirstDraw") }
        verify(exactly = 1) { tracers.startSpan(activity2, "TimeToFirstDraw") }
        verify(exactly = 1) { firstDrawListener.registerFirstDraw(activity1, any()) }
        verify(exactly = 1) { firstDrawListener.registerFirstDraw(activity2, any()) }
    }
}
