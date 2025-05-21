package software.amazon.opentelemetry.android.otlp

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * kotlinx.Serialization-compatible data types that roughly map out the OTLP Traces spec in JSON:
 * ref: https://github.com/open-telemetry/opentelemetry-proto/blob/v1.7.0/examples/trace.json
 */

@Serializable
data class TraceRoot(
    @Required val resourceSpans: List<ResourceSpan>
) {
    fun findSpansByName(name: String): List<Span> =
        resourceSpans
            .flatMap { it.scopeSpans }
            .flatMap { it.spans }
            .filter { it.name == name }

    fun validateResourceAttributesExist(attributes: Map<String, String>): Boolean =
        resourceSpans.all { resourceSpan ->
            attributes.all { (key, value) ->
                resourceSpan.resource.attributes
                    .any { it.key == key && it.value.stringValue == value }
            }
        }
}

@Serializable
data class ResourceSpan(
    @Required val resource: Resource,
    @Required val scopeSpans: List<ScopeSpan>
)

@Serializable
data class ScopeSpan(
    @Required val scope: Scope,
    @Required val spans: List<Span>
)

@Serializable
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
    val events: List<SpanEvent>? = null
)

@Serializable
data class SpanEvent(
    val timeUnixNano: String,
    val name: String
)