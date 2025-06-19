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
package software.amazon.opentelemetry.android.api

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import software.amazon.opentelemetry.android.api.internal.AwsRumImpl
import software.amazon.opentelemetry.android.api.internal.AwsRumSdkApi

object AwsRum : AwsRumSdkApi {
    private val impl: AwsRumImpl = AwsRumImpl()

    override fun getTracer(instrumentationScope: String): Tracer = impl.getTracer(instrumentationScope)

    override fun startSpan(
        name: String,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = impl.startSpan(name, screenName, attributes, spanKind)

    override fun startSpan(
        name: String,
        startTimeMs: Long,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = impl.startSpan(name, startTimeMs, screenName, attributes, spanKind)

    override fun startChildSpan(
        name: String,
        parent: Span,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = impl.startChildSpan(name, parent, screenName, attributes, spanKind)

    override fun startChildSpan(
        name: String,
        parent: Span,
        startTimeMs: Long,
        screenName: String?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
    ): Span = impl.startChildSpan(name, parent, startTimeMs, screenName, attributes, spanKind)

    override fun <T> executeSpan(
        name: String,
        screenName: String?,
        parent: Span?,
        attributes: Map<String, Any>?,
        spanKind: SpanKind?,
        codeBlock: (Span) -> T,
    ): T = impl.executeSpan(name, screenName, parent, attributes, spanKind, codeBlock)

    override fun startFragmentTTFDSpan(fragmentName: String): Span = impl.startFragmentTTFDSpan(fragmentName)
}
