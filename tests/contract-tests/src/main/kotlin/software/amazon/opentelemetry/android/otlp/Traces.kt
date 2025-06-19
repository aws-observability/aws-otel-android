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
package software.amazon.opentelemetry.android.otlp

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * kotlinx.Serialization-compatible data types that roughly map out the OTLP Traces spec in JSON:
 * ref: https://github.com/open-telemetry/opentelemetry-proto/blob/v1.7.0/examples/trace.json
 */

@Serializable
data class TraceRoot(
    @Required val resourceSpans: List<ResourceSpan>,
)

@Serializable
data class ResourceSpan(
    @Required val resource: Resource,
    @Required val scopeSpans: List<ScopeSpan>,
)

@Serializable
data class ScopeSpan(
    @Required val scope: Scope,
    @Required val spans: List<Span>,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Span(
    val traceId: String,
    val spanId: String,
    val parentSpanId: String,
    val flags: Int,
    val name: String,
    val kind: Int,
    val startTimeUnixNano: String,
    val endTimeUnixNano: String,
    val attributes: List<Attribute>,
    val droppedAttributesCount: Int? = null,
    val events: List<SpanEvent>? = null,
)

@Serializable
@JsonIgnoreUnknownKeys
data class SpanEvent(
    val timeUnixNano: String,
    val name: String,
)
