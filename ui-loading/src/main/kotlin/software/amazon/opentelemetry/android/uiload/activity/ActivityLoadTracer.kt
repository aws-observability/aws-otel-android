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
import android.view.View
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.android.instrumentation.common.DefaultScreenNameExtractor
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import software.amazon.opentelemetry.android.uiload.utils.getComplexity

class ActivityLoadTracer(
    private val tracer: Tracer,
) {
    companion object {
        val ACTIVITY_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("activity.name")
        const val SCREEN_VIEW_NODES = "screen.view.nodes"
        const val SCREEN_VIEW_DEPTH = "screen.view.depth"
    }

    private val tracersByActivity: MutableMap<Activity, PerActivityLoadingTracer> =
        HashMap<Activity, PerActivityLoadingTracer>()

    fun startSpan(
        activity: Activity,
        spanName: String,
    ): Span {
        if (getTracer(activity).span != null) {
            return getTracer(activity).span!!
        }
        val activityName = activity.javaClass.getSimpleName()
        val spanBuilder =
            getTracer(activity)
                .tracer
                .spanBuilder(spanName)
                .setAttribute<String>(ACTIVITY_NAME_KEY, activityName)
        val span = spanBuilder.startSpan()
        span.setAttribute<String?>(
            RumConstants.SCREEN_NAME_KEY,
            DefaultScreenNameExtractor.extract(activity),
        )
        getTracer(activity).span = span
        return span
    }

    fun endSpan(
        activity: Activity,
        view: View,
    ) {
        val existingSpan = getTracer(activity).span
        if (existingSpan != null) {
            val (count, depth) = view.getComplexity()
            existingSpan.setAttribute(SCREEN_VIEW_NODES, count.toLong())
            existingSpan.setAttribute(SCREEN_VIEW_DEPTH, depth.toLong())
            existingSpan.end()
        }
        getTracer(activity).span = null
    }

    private fun getTracer(activity: Activity): PerActivityLoadingTracer {
        var activityTracer: PerActivityLoadingTracer? =
            tracersByActivity[activity]
        if (activityTracer == null) {
            activityTracer =
                PerActivityLoadingTracer(
                    tracer,
                    activity,
                    null,
                )
            tracersByActivity.put(activity, activityTracer)
        }
        return activityTracer
    }

    private data class PerActivityLoadingTracer(
        val tracer: Tracer,
        val activity: Activity,
        var span: Span?,
    )
}
