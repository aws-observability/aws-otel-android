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

import android.app.Application
import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.android.instrumentation.InstallationContext
import io.opentelemetry.api.trace.Tracer
import software.amazon.opentelemetry.android.uiload.utils.CommonUtils.getVersionSDKInt
import software.amazon.opentelemetry.android.uiload.utils.FirstDrawListener

class ActivityLoadInstrumentation : AndroidInstrumentation {
    companion object {
        const val INSTRUMENTATION_SCOPE: String = "software.amazon.opentelemetry.ui-loading"
    }

    override fun install(ctx: InstallationContext) {
        ctx.application.registerActivityLifecycleCallbacks(buildActivityLoadTracer(ctx))
    }

    private fun buildActivityLoadTracer(ctx: InstallationContext): Application.ActivityLifecycleCallbacks {
        val delegateTracer: Tracer = ctx.openTelemetry.getTracer(INSTRUMENTATION_SCOPE)
        val tracers = ActivityLoadTracer(delegateTracer)
        return if (getVersionSDKInt() < 29) {
            Pre29ActivityLoadCallback(
                tracers,
                FirstDrawListener(),
            )
        } else {
            ActivityLoadCallback(
                tracers,
                FirstDrawListener(),
            )
        }
    }
}
