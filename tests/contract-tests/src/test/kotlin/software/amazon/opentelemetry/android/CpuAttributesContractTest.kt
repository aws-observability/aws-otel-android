package software.amazon.opentelemetry.android

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OtlpResolver::class)
class CpuAttributesContractTest {
    companion object {
        const val APP_NAME = "Agent Demo Application"
    }

    @Test
    fun `Spans with duration should have cpu utilization attributes`(data: ParsedOtlpData) {
        val appSpans = data.traces.spans().filter {
            it.attributes.has("service.name", APP_NAME)
        }

        Assertions.assertTrue(
            appSpans.all { span ->
                !span.hasDuration() || span.attributes.has("process.cpu.avg_utilization")
            }
        )
    }
}