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

@Serializable
data class LogRoot(
    @Required val resourceLogs: List<ResourceLog>,
)

@Serializable
data class ResourceLog(
    @Required val resource: Resource,
    @Required val scopeLogs: List<ScopeLog>,
)

@Serializable
data class ScopeLog(
    @Required val scope: Scope,
    @Required val logRecords: List<LogRecord>,
)

@Serializable
data class LogRecord(
    val timeUnixNano: String = "",
    val observedTimeUnixNano: String = "",
    @Required val attributes: List<Attribute>,
    val traceId: String = "",
    val spanId: String = "",
)
