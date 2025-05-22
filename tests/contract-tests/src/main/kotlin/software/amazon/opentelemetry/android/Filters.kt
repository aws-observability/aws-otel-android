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

import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.Resource
import software.amazon.opentelemetry.android.otlp.Span
import software.amazon.opentelemetry.android.otlp.TraceRoot

fun List<TraceRoot>.findSpansByName(name: String): List<Span> =
    this
        .flatMap { it.resourceSpans }
        .flatMap { it.scopeSpans }
        .flatMap { it.spans }
        .filter { it.name == name }

@JvmName("traceRootResources")
fun List<TraceRoot>.resources(): List<Resource> =
    this
        .flatMap { it.resourceSpans }
        .map { it.resource }

@JvmName("logRootResources")
fun List<LogRoot>.resources(): List<Resource> =
    this
        .flatMap { it.resourceLogs }
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
