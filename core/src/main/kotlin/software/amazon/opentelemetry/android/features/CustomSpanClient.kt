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

import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.common.Constants

/**
 * A convenient client for custom spans. This method will begin a span in the current thread context
 */
inline fun OpenTelemetryRumClient.Companion.span(
    name: String,
    scopeName: String = Constants.CUSTOM_TRACER_NAME,
    attributes: Attributes? = null,
    screenName: String? = null,
    spanKind: SpanKind? = SpanKind.INTERNAL,
    block: (Span) -> Unit,
) {
    // If there is no instance, just run the code block without creating a span
    val instance = this.getInstance()
    if (instance == null) {
        block(Span.getInvalid())
        return
    }

    val tracer = instance.openTelemetry.getTracer(scopeName)
    val builder = tracer.spanBuilder(name)
    spanKind?.let { builder.setSpanKind(spanKind) }

    val span = builder.startSpan()

    attributes?.let { span.setAllAttributes(attributes) }
    screenName?.let { span.setAttribute(RumConstants.SCREEN_NAME_KEY, screenName) }

    span.makeCurrent().use {
        try {
            block(span)
        } catch (t: Throwable) {
            span.recordException(t)
            span.setStatus(StatusCode.ERROR)
            throw t
        } finally {
            span.end()
        }
    }
}

/**
 * Create a span for tracking Fragment Time to First Draw (TTFD).
 * This span should be created in fragments then ended when the first frame is drawn.
 */
fun OpenTelemetryRumClient.Companion.fragmentTTFDSpan(fragmentName: String): Span {
    // If there is no running instance, return a no-op span
    val instance = this.getInstance()
    if (instance == null) {
        return Span.getInvalid()
    }

    val tracer = instance.openTelemetry.getTracer(Constants.CUSTOM_TRACER_NAME)
    val builder = tracer.spanBuilder(Constants.TIME_TO_FIRST_DRAW_SPAN_NAME)
    builder.setSpanKind(SpanKind.INTERNAL)

    val span = builder.startSpan()
    span.setAttribute(Constants.FRAGMENT_NAME_KEY, fragmentName)

    return span
}
