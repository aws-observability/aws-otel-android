package software.amazon.opentelemetry.android.otlp

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class Resource(
    @Required val attributes: List<Attribute>
)

@Serializable
data class Attribute(
    @Required val key: String,
    val value: Value
)

@Serializable
data class Value(
    val stringValue: String? = null
)

@Serializable
data class Scope(
    @Required val name: String
)