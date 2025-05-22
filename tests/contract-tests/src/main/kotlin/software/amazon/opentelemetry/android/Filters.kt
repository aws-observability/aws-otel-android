package software.amazon.opentelemetry.android

import software.amazon.opentelemetry.android.otlp.Span
import software.amazon.opentelemetry.android.otlp.TraceRoot

fun List<TraceRoot>.findSpansByName(name: String): List<Span> =
    this.flatMap { it.resourceSpans }
        .flatMap { it.scopeSpans }
        .flatMap { it.spans }
        .filter { it.name == name }

fun List<TraceRoot>.resourceAttributesExist(attributes: Map<String, String>): Boolean =
    this.all { traceRoot ->
        traceRoot.resourceSpans.all { resourceSpan ->
            attributes.all { (key, value) ->
                resourceSpan.resource.attributes
                    .any { it.key == key && it.value.stringValue == value }
            }
        }
    }

fun List<TraceRoot>.resourceAttributeKeyExists(keyName: String): Boolean =
    this.all { traceRoot ->
        traceRoot.resourceSpans.all { resourceSpan ->
            resourceSpan.resource.attributes.find { it.key == keyName } != null
        }
    }