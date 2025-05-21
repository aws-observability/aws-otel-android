package software.amazon.opentelemetry.android.otlp.parser

import kotlinx.serialization.json.Json
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.TraceRoot
import java.io.File

object OtlpFileParser {

    fun readTracesFile(file: File): List<TraceRoot> {
        val logRoots = mutableListOf<TraceRoot>()
        file.useLines { line ->
            val root = Json.decodeFromString<TraceRoot>(line.toString())
            logRoots.add(root)
        }
        return logRoots
    }

    fun readLogsFile(file: File): List<LogRoot> {
        val logRoots = mutableListOf<LogRoot>()
        file.useLines { line ->
            val root = Json.decodeFromString<LogRoot>(line.toString())
            logRoots.add(root)
        }
        return logRoots
    }
}