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
package software.amazon.opentelemetry.android.uiload.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.opentelemetry.android.uiload.utils.CommonUtils.getVersionSDKInt

class FirstDrawListenerTest {
    @MockK
    private lateinit var activity: Activity

    @MockK
    private lateinit var window: Window

    @MockK
    private lateinit var decorView: View

    @MockK
    private lateinit var viewTreeObserver: ViewTreeObserver

    @MockK
    private lateinit var windowCallback: Window.Callback

    @MockK
    private lateinit var mainLooper: Looper

    private lateinit var firstDrawListener: FirstDrawListener

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mainLooper

        mockkConstructor(Handler::class)
        every { anyConstructed<Handler>().post(any()) } answers {
            (firstArg<Runnable>()).run()
            true
        }

        every { activity.window } returns window
        every { window.callback } returns windowCallback
        every { window.decorView } returns decorView
        every { decorView.viewTreeObserver } returns viewTreeObserver
        every { viewTreeObserver.isAlive } returns true
        every { decorView.isAttachedToWindow } returns true
        every { viewTreeObserver.addOnDrawListener(any()) } just runs
        every { viewTreeObserver.removeOnDrawListener(any()) } just runs
        every { window.peekDecorView() } returns decorView

        firstDrawListener = FirstDrawListener()
    }

    @Test
    fun `test register first draw with DecorView`() {
        var callbackCalled = false

        firstDrawListener.registerFirstDraw(activity) {
            callbackCalled = true
        }

        val drawListenerSlot = slot<ViewTreeObserver.OnDrawListener>()

        verify { viewTreeObserver.addOnDrawListener(capture(drawListenerSlot)) }

        drawListenerSlot.captured.onDraw()

        assertTrue(callbackCalled)
    }

    @Test
    fun `test register first draw on legacy version with unattached view`() {
        mockkObject(CommonUtils)
        every { getVersionSDKInt() } returns 25
        every { decorView.isAttachedToWindow } returns false

        val attachListenerSlot = slot<View.OnAttachStateChangeListener>()
        every { decorView.addOnAttachStateChangeListener(capture(attachListenerSlot)) } just runs
        every { decorView.removeOnAttachStateChangeListener(any()) } just runs

        var callbackCalled = false

        firstDrawListener.registerFirstDraw(activity) {
            callbackCalled = true
        }

        attachListenerSlot.captured.onViewAttachedToWindow(decorView)

        val drawListenerSlot = slot<ViewTreeObserver.OnDrawListener>()
        verify { viewTreeObserver.addOnDrawListener(capture(drawListenerSlot)) }

        drawListenerSlot.captured.onDraw()

        assertTrue(callbackCalled)
    }

    @Test
    fun `test FirstDrawListener removes itself after drawing`() {
        firstDrawListener.registerFirstDraw(activity) { }

        val drawListenerSlot = slot<ViewTreeObserver.OnDrawListener>()
        verify { viewTreeObserver.addOnDrawListener(capture(drawListenerSlot)) }

        drawListenerSlot.captured.onDraw()

        verify { viewTreeObserver.removeOnDrawListener(any()) }
    }

    @Test
    fun `test FirstDrawListener only invokes callback once`() {
        var callbackCount = 0

        firstDrawListener.registerFirstDraw(activity) {
            callbackCount++
        }

        val drawListenerSlot = slot<ViewTreeObserver.OnDrawListener>()
        verify { viewTreeObserver.addOnDrawListener(capture(drawListenerSlot)) }

        drawListenerSlot.captured.onDraw()
        drawListenerSlot.captured.onDraw()

        assertEquals(1, callbackCount)
    }
}
