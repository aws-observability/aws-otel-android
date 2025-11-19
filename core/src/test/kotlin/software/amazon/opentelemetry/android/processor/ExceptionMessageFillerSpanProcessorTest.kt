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
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.semconv.ExceptionAttributes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExceptionMessageFillerSpanProcessorTest {
    @MockK
    private lateinit var mockSpan: ReadWriteSpan

    private val processor = ExceptionMessageFillerSpanProcessor()

    @BeforeEach
    fun setup() {
        every { mockSpan.setAttribute(any<AttributeKey<String>>(), any<String>()) } returns mockSpan
    }

    @Test
    fun `onEnding should set exception message to type only when message is missing`() {
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns null
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns "NullPointerException"

        processor.onEnding(mockSpan)

        verify {
            mockSpan.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "NullPointerException")
        }
    }

    @Test
    fun `onEnding should combine type and message when both are present`() {
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns "Object was null"
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns "NullPointerException"

        processor.onEnding(mockSpan)

        verify {
            mockSpan.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "NullPointerException: Object was null")
        }
    }

    @Test
    fun `onEnding should not set exception message when type is missing`() {
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_MESSAGE.key)) } returns "Some message"
        every { mockSpan.getAttribute(AttributeKey.stringKey(ExceptionAttributes.EXCEPTION_TYPE.key)) } returns null

        processor.onEnding(mockSpan)

        verify(exactly = 0) {
            mockSpan.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, any<String>())
        }
    }
}
