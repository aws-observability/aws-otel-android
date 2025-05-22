package software.amazon.opentelemetry.android

import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.Resource
import software.amazon.opentelemetry.android.otlp.Span
import software.amazon.opentelemetry.android.otlp.TraceRoot

fun List<TraceRoot>.findSpansByName(name: String): List<Span> =
    this.flatMap { it.resourceSpans }
        .flatMap { it.scopeSpans }
        .flatMap { it.spans }
        .filter { it.name == name }

fun List<TraceRoot>.resources(): List<Resource> =
    this.flatMap { it.resourceSpans }
        .map { it.resource }

fun List<LogRoot>.resources(): List<Resource> =
    this.flatMap { it.resourceLogs }
        .map { it.resource }

fun List<Resource>.attributeKeyExists(keyName: String): Boolean =
    this.all { resource ->
        resource.attributes.find { it.key == keyName } != null
    }

fun List<Resource>.validateResourceAttributes(attributes: Map<String, String>): Boolean =
    this.any { resource ->
        attributes.all { (key, value) ->
            resource.attributes
                .all { it.key == key && it.value.stringValue == value }
        }
    }