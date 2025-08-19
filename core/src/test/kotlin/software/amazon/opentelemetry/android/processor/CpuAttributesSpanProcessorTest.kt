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

import android.os.Process
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CpuAttributesSpanProcessorTest {
    @MockK
    private lateinit var mockSpan: ReadWriteSpan

    @MockK
    private lateinit var context: Context

    val processor = CpuAttributesSpanProcessor(cpuCores = 1)

    @BeforeEach
    fun setup() {
        mockkStatic(Process::class)
        every { mockSpan.setAttribute(any<String>(), any<Double>()) } returns mockSpan
        every { mockSpan.setAttribute(any<String>(), any<Long>()) } returns mockSpan
    }

    @Test
    fun `onStart should set the right attribute`() {
        every { Process.getElapsedCpuTime() } returns 5L

        processor.onStart(context, mockSpan)

        verify {
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_START, 5L)
        }
    }

    @Test
    fun `onEnding should set the right attributes if span has duration`() {
        every { Process.getElapsedCpuTime() } returns 50L
        every {
            mockSpan.getAttribute(
                AttributeKey.longKey(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_START),
            )
        } returns 5L
        // cpuTime = 45

        every { mockSpan.latencyNanos } returns 100L * 1_000_000

        // Span took 100ms, process was active for 45ms of that time. Therefore, expect 45% cpu
        processor.onEnding(mockSpan)

        verify {
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_AVERAGE_ATTRIBUTE, 45.0)
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_END, 50L)
        }

        // With multiple cores, divide CPU average, expect 22.5% cpu
        val moreCoresProcessor = CpuAttributesSpanProcessor(cpuCores = 2)
        moreCoresProcessor.onEnding(mockSpan)

        verify {
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_AVERAGE_ATTRIBUTE, 22.5)
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_END, 50L)
        }
    }

    @Test
    fun `onEnding should not set CPU average attribute if span has zero duration`() {
        every { Process.getElapsedCpuTime() } returns 50L
        every {
            mockSpan.getAttribute(
                AttributeKey.longKey(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_START),
            )
        } returns 5L

        every { mockSpan.latencyNanos } returns 0

        processor.onEnding(mockSpan)

        verify(exactly = 0) {
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_AVERAGE_ATTRIBUTE, any<Double>())
        }

        verify {
            mockSpan.setAttribute(CpuAttributesSpanProcessor.CPU_ELAPSED_TIME_END, 50L)
        }
    }
}
