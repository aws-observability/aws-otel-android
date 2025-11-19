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
package software.amazon.opentelemetry.android.processor

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.logs.ReadWriteLogRecord
import io.opentelemetry.semconv.ExceptionAttributes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExceptionMessageFillerLogRecordProcessorTest {
    @MockK
    private lateinit var mockLogRecord: ReadWriteLogRecord

    @MockK
    private lateinit var context: Context

    private val processor = ExceptionMessageFillerLogRecordProcessor()

    @BeforeEach
    fun setup() {
        every { mockLogRecord.setAttribute(any<AttributeKey<String>>(), any<String>()) } returns mockLogRecord
    }

    @Test
    fun `onEmit should set exception message to type only when message is missing`() {
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns null
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns "IllegalStateException"

        processor.onEmit(context, mockLogRecord)

        verify {
            mockLogRecord.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "IllegalStateException")
        }
    }

    @Test
    fun `onEmit should combine type and message when both are present`() {
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns
            "Something went wrong"
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns "IllegalStateException"

        processor.onEmit(context, mockLogRecord)

        verify {
            mockLogRecord.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "IllegalStateException: Something went wrong")
        }
    }

    @Test
    fun `onEmit should not set exception message when type is missing`() {
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns "Some message"
        every { mockLogRecord.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns null

        processor.onEmit(context, mockLogRecord)

        verify(exactly = 0) {
            mockLogRecord.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, any<String>())
        }
    }
}
