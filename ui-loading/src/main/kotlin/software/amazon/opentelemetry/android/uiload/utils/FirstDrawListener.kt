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
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import software.amazon.opentelemetry.android.uiload.utils.CommonUtils.getVersionSDKInt

/**
 * Listener that listen on when the first frame of the Activity has been drawn and unregister itself
 * when the first draw is done
 *
 */
class FirstDrawListener {
    fun registerFirstDraw(
        activity: Activity,
        drawDoneCallback: () -> Unit,
    ) {
        val window = activity.window
        window.onDecorViewReady {
            val decorView = window.decorView
            if (getVersionSDKInt() < Build.VERSION_CODES.O &&
                !(decorView.viewTreeObserver.isAlive && decorView.isAttachedToWindow)
            ) {
                decorView.addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            decorView.viewTreeObserver.addOnDrawListener(NextDrawListener(decorView, drawDoneCallback))
                            decorView.removeOnAttachStateChangeListener(this)
                        }

                        override fun onViewDetachedFromWindow(v: View) = Unit
                    },
                )
            } else {
                decorView.viewTreeObserver.addOnDrawListener(NextDrawListener(decorView, drawDoneCallback))
            }
        }
    }

    fun Window.onDecorViewReady(callback: () -> Unit) {
        if (peekDecorView() == null) {
            onContentChanged {
                callback()
                return@onContentChanged false
            }
        } else {
            callback()
        }
    }

    fun Window.onContentChanged(callbackInvocation: () -> Boolean) {
        val currentCallback = callback
        val callback =
            if (currentCallback is WindowDelegateCallback) {
                currentCallback
            } else {
                val newCallback = WindowDelegateCallback(currentCallback)
                callback = newCallback
                newCallback
            }
        callback.onContentChangedCallbacks += callbackInvocation
    }

    /**
     * ViewTreeObserver.removeOnDrawListener() cannot be called from the onDraw() callback, so remove it in next draw.
     * This NextDrawListener implementation is based on the deep-dive work from the Pierre-Yves Ricau and his blog (https://dev.to/pyricau/android-vitals-first-draw-time-m1d)
     */
    private class NextDrawListener(
        val view: View,
        val drawDoneCallback: () -> Unit,
    ) : ViewTreeObserver.OnDrawListener {
        val handler = Handler(Looper.getMainLooper())
        var invoked = false

        override fun onDraw() {
            if (!invoked) {
                invoked = true
                drawDoneCallback()
                handler.post {
                    if (view.viewTreeObserver.isAlive) {
                        view.viewTreeObserver.removeOnDrawListener(this)
                    }
                }
            }
        }
    }

    private class WindowDelegateCallback(
        private val delegate: Window.Callback,
    ) : Window.Callback by delegate {
        val onContentChangedCallbacks = mutableListOf<() -> Boolean>()

        override fun onContentChanged() {
            onContentChangedCallbacks.removeAll { callback ->
                !callback()
            }
            delegate.onContentChanged()
        }
    }
}
