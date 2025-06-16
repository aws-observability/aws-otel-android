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

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer

internal interface AwsRumSpanApi {
    /**
     * Get a tracer with the specified instrumentation scope name.
     * This method provides direct access to OpenTelemetry tracer for advanced use cases.
     *
     * @param instrumentationScope The name identifying the instrumentation scope. If not provided, the default scope will be used.
     * @return An OpenTelemetry [Tracer] instance that can be used to create spans.
     *
     * Example usage:
     * ```
     * val customTracer = AwsRum.getInstance().getTracer("network-operations")
     * val span = customTracer.spanBuilder("http-request")
     *     .setAttribute("http.method", "GET")
     *     .startSpan()
     * try {
     *     // Perform network operation
     * } finally {
     *     span.end()
     * }
     * ```
     *
     * Note: For most use cases, prefer using [startSpan] or [startChildSpan] instead of creating spans directly
     * with the tracer. Direct tracer access should only be used when you need more control over tracer scope and span creation.
     */
    fun getTracer(instrumentationScope: String = Constants.TraceScope.DEFAULT): Tracer

    /**
     * Create and automatically start a custom span.
     *
     * @param name The name of the span.
     * @param screenName Optional name of the screen where the span is created. If not provided,
     *                  the current screen name will be automatically detected which is last resumed activity or fragment.
     *                  View details: https://github.com/open-telemetry/opentelemetry-android/blob/20e253b87e85fe6f7fe0f52654dacab48186f138/services/src/main/java/io/opentelemetry/android/internal/services/visiblescreen.
     * @param attributes Optional map of key-value pairs to add as span attributes.
     *                  Supported value types are String, Boolean, Int, Long, and Double. Other types will be ignored.
     * @return The started span, which must be ended by calling [Span.end] when the operation completes.
     *
     * Example usage:
     * ```
     * val span = AwsRum.getInstance().startSpan(
     *     name = "load_user_data",
     *     screenName = "UserProfile",
     *     attributes = mapOf(
     *         "user_id" to "12345",
     *         "count" to 4,
     *         "visible" to true
     *     )
     * )
     * span.end()
     * ```
     */
    fun startSpan(
        name: String,
        screenName: String? = null,
        attributes: Map<String, Any>? = null,
    ): Span

    /**
     * Create and automatically start a child span with a parent span.
     * Child spans are used to track sub-operations within a larger operation.
     *
     * @param name The name of the child span.
     * @param parent The parent span that this span will be associated with.
     * @param screenName Optional name of the screen where the span is created. If not provided,
     *                  the current screen name will be automatically detected.
     * @param attributes Optional map of key-value pairs to add as span attributes.
     *                  Supported value types are String, Boolean, Int, Long, and Double. Other types will be ignored.
     * @return The started child span, which must be ended by calling [Span.end] when the operation completes.
     */
    fun startChildSpan(
        name: String,
        parent: Span,
        screenName: String? = null,
        attributes: Map<String, Any>? = null,
    ): Span

    /**
     * Create a span for tracking Fragment Time To First Draw (TTFD).
     * This span should be created in fragments and ended when the first frame is drawn.
     *
     * @param fragmentName The name of the fragment being tracked (typically fragment.javaClass.simpleName)
     * @return A span that should be ended when the fragment's first frame is drawn
     */
    fun startFragmentTTFDSpan(fragmentName: String): Span
}
