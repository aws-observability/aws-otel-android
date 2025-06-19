package software.amazon.opentelemetry.android

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(OtlpResolver::class)
class UserIdContractTest {
    companion object {
        const val USER_ID_ATTR = "user.id"
    }

    @Test
    fun `Spans and logs should have a userid attribute`(data: ParsedOtlpData) {
        Assertions.assertTrue(
            data.traces.spans().all { span -> span.attributes.has(USER_ID_ATTR) }
        )
        Assertions.assertTrue(
            data.logs.logRecords().all { span -> span.attributes.has(USER_ID_ATTR) }
        )
    }
}