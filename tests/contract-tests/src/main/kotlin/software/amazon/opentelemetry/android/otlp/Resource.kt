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

@Serializable
data class Resource(
    @Required val attributes: List<Attribute>,
)

@Serializable
data class Attribute(
    @Required val key: String,
    val value: Value,
)

@Serializable
data class Value(
    val stringValue: String? = null,
    val doubleValue: Double? = null,
    val intValue: String? = null,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Scope(
    @Required val name: String,
)
