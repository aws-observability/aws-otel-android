package software.amazon.opentelemetry.android

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.opentelemetry.android.otlp.LogRoot
import software.amazon.opentelemetry.android.otlp.TraceRoot
import software.amazon.opentelemetry.android.otlp.parser.OtlpFileParser
import java.io.File

class SessionContractTest {

    val logs: List<LogRoot> = OtlpFileParser.readLogsFile(File("logs.txt"))

    val traces: List<TraceRoot> = OtlpFileParser.readTracesFile(File("traces.txt"))

    @Test
    fun `spans and logs should have a random sessionid in attributes`() {
        Assertions.assertTrue(
            traces.resourceAttributeKeyExists("session.id")
        )
    }
}