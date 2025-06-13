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
package software.amazon.opentelemetry.android

import software.amazon.opentelemetry.android.otlp.Attribute
import software.amazon.opentelemetry.android.otlp.LogRecord
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.Resource
import software.amazon.opentelemetry.android.otlp.ScopeLog
import software.amazon.opentelemetry.android.otlp.ScopeSpan
import software.amazon.opentelemetry.android.otlp.Span
import software.amazon.opentelemetry.android.otlp.TraceRoot
import software.amazon.opentelemetry.android.otlp.Value

fun List<TraceRoot>.findSpansByName(name: String): List<Span> =
    this.flatMap { it.resourceSpans }.flatMap { it.scopeSpans }.flatMap { it.spans }.filter {
        it.name == name
    }

@JvmName("traceRootResources")
fun List<TraceRoot>.resources(): List<Resource> = this.flatMap { it.resourceSpans }.map { it.resource }

fun List<TraceRoot>.scopeSpans(name: String): List<ScopeSpan> =
    this
        .flatMap { it.resourceSpans }
        .flatMap { it.scopeSpans }
        .filter { it.scope.name == name }

fun List<TraceRoot>.spans(): List<Span> =
    this
        .flatMap { it.resourceSpans }
        .flatMap { it.scopeSpans }
        .flatMap { it.spans }

fun List<ScopeSpan>.spans(name: String): List<Span> =
    this
        .flatMap { it.spans }
        .filter { it.name == name }

fun List<ScopeSpan>.spans(
    name: String,
    attributes: Map<String, String>,
): List<Span> =
    this
        .flatMap { it.spans }
        .filter { it.name == name }
        .filter { it.attributes.has(attributes) }

fun Span.findSpanEvents(spanEventNames: List<String>): Boolean =
    this.events != null &&
        spanEventNames.all { event ->
            this.events.find { it.name == event } != null
        }

@JvmName("logRootResources")
fun List<LogRoot>.resources(): List<Resource> = this.flatMap { it.resourceLogs }.map { it.resource }

fun List<Resource>.attributeKeyExists(keyName: String): Boolean =
    this.all { resource -> resource.attributes.find { it.key == keyName } != null }

fun List<Resource>.validateResourceAttributes(attributes: Map<String, String>): Boolean =
    this.any { resource ->
        attributes.all { (key, value) ->
            resource.attributes.all { it.key == key && it.value.stringValue == value }
        }
    }

fun List<LogRoot>.logRecords(): List<LogRecord> =
    this
        .flatMap { it.resourceLogs }
        .flatMap { it.scopeLogs }
        .flatMap { it.logRecords }

fun List<Attribute>.has(keyName: String): Boolean = this.any { it.key == keyName }

fun List<Attribute>.has(
    keyName: String,
    value: String,
): Boolean = this.any { it.key == keyName && it.value.stringValue == value }

fun List<Attribute>.getValue(keyName: String): Value = this.first { it.key == keyName }.value

fun List<Attribute>.has(compareAttributes: Map<String, String>): Boolean =
    this.let { attributes ->
        compareAttributes.all { (key, value) ->
            attributes.any { it.key == key && it.value.stringValue == value }
        }
    }

fun List<LogRoot>.scopeLogs(name: String): List<ScopeLog> =
    this
        .flatMap { it.resourceLogs }
        .flatMap { it.scopeLogs }
        .filter { it.scope.name == name }

@JvmName("scopeLogToLogRecords")
fun List<ScopeLog>.logRecords(): List<LogRecord> =
    this
        .flatMap { it.logRecords }

fun List<LogRecord>.attributes(keyName: String): Attribute =
    this
        .flatMap { it.attributes }
        .filter { it.key == keyName }
        .first()

fun Span.getAttributes(keyName: String): Attribute =
    this
        .attributes
        .filter { it.key == keyName }
        .first()

@JvmName("spanToAttributes")
fun List<Span>.attributes(keyName: String): Attribute =
    this
        .flatMap { it.attributes }
        .filter { it.key == keyName }
        .first()
