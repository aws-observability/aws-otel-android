package software.amazon.opentelemetry.android.otlp

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class LogRoot(
    @Required val resourceLogs: List<ResourceLog>
)

@Serializable
data class ResourceLog(
    @Required val resource: Resource,
    @Required val scopeLogs: List<ScopeLog>
)

@Serializable
data class ScopeLog(
    @Required val scope: Scope,
    @Required val logRecords: List<LogRecord>
)

@Serializable
data class LogRecord(
    val timeUnixNano: String = "",
    val observedTimeUnixNano: String = "",
    @Required val attributes: List<Attribute>,
    val traceId: String = "",
    val spanId: String = ""
)
