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
package software.amazon.opentelemetry.android.api.internal

import io.opentelemetry.android.common.RumConstants.SCREEN_NAME_KEY
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.api.internal.Constants.FRAGMENT_NAME_KEY
import software.amazon.opentelemetry.android.api.internal.Constants.SpanName.TIME_TO_FIRST_DRAW
import java.util.concurrent.TimeUnit

internal class AwsRumSpanApiImpl(
    private val openTelemetryAgent: OpenTelemetryAgent = getDefaultAgent(),
) : AwsRumSpanApi {
    companion object {
        private fun getDefaultAgent() = OpenTelemetryAgent.getOpenTelemetryAgent()!!
    }

    override fun getTracer(instrumentationScope: String): Tracer = openTelemetryAgent.getOpenTelemetry().getTracer(instrumentationScope)

    override fun startSpan(
        name: String,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = startSpanInternal(name, screenName = screenName, attributes = attributes, spanKind = spanKind)

    override fun startSpan(
        name: String,
        startTimeMs: Long,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = startSpanInternal(name, screenName = screenName, attributes = attributes, spanKind = spanKind, startTimeMs = startTimeMs)

    override fun startChildSpan(
        name: String,
        parent: Span,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = startSpanInternal(name, parentSpan = parent, screenName = screenName, attributes = attributes, spanKind = spanKind)

    override fun startChildSpan(
        name: String,
        parent: Span,
        startTimeMs: Long,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span =
        startSpanInternal(
            name,
            parentSpan = parent,
            screenName = screenName,
            attributes = attributes,
            spanKind = spanKind,
            startTimeMs = startTimeMs,
        )

    override fun <T> executeSpan(
        name: String,
        screenName: String?,
        parent: Span?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
        codeBlock: (Span) -> T,
    ): T {
        val span = startSpanInternal(name, attributes = attributes, parentSpan = parent, screenName = screenName, spanKind = spanKind, null)

        try {
            return codeBlock(span)
        } catch (t: Throwable) {
            span.setStatus(StatusCode.ERROR)
            throw t
        } finally {
            span.end()
        }
    }

    override fun startFragmentTTFDSpan(fragmentName: String): Span {
        val span =
            getTracer(Constants.TraceScope.AWS_RUM_CUSTOM_TRACER)
                .spanBuilder(TIME_TO_FIRST_DRAW)
                .setAttribute(FRAGMENT_NAME_KEY, fragmentName)
                .setSpanKind(SpanKind.CLIENT)
                .startSpan()
        // Override the default screen.name set by RumAttributeAppender
        span.setAttribute(SCREEN_NAME_KEY, fragmentName)
        return span
    }

    private fun startSpanInternal(
        name: String,
        attributes: Map<String, Any>? = null,
        parentSpan: Span? = null,
        screenName: String? = null,
        spanKind: SpanKind? = null,
        startTimeMs: Long? = null,
    ): Span {
        val spanBuilder = getTracer(Constants.TraceScope.AWS_RUM_CUSTOM_TRACER).spanBuilder(name)

        parentSpan?.let { parent ->
            val parentContext = parent.storeInContext(Context.current())
            spanBuilder.setParent(parentContext)
        }
        spanKind?.let { spanBuilder.setSpanKind(spanKind) }

        startTimeMs?.let {
            // Convert to nanoseconds to align with default sdk behavior
            val msToNs = TimeUnit.MILLISECONDS.toNanos(startTimeMs)
            spanBuilder.setStartTimestamp(msToNs, TimeUnit.NANOSECONDS)
        }

        val span = spanBuilder.startSpan()

        screenName?.let { span.setAttribute(SCREEN_NAME_KEY, screenName) }

        attributes?.forEach { (key, value) ->
            when (value) {
                // Other types are ignored
                is String -> span.setAttribute(key, value)
                is Boolean -> span.setAttribute(key, value)
                is Long -> span.setAttribute(key, value)
                is Double -> span.setAttribute(key, value)
                is Int -> span.setAttribute(key, value.toLong())
            }
        }
        return span
    }
}
