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
package software.amazon.opentelemetry.android.api

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.api.internal.AwsRumSpanApiImpl
import software.amazon.opentelemetry.android.api.internal.Constants
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class AwsRumSpanApiImplTest {
    @MockK
    private lateinit var mockOpenTelemetryAgent: OpenTelemetryAgent

    @MockK
    private lateinit var mockOpenTelemetry: OpenTelemetry

    @MockK
    private lateinit var mockTracer: Tracer

    @MockK
    private lateinit var mockSpanBuilder: SpanBuilder

    @RelaxedMockK
    private lateinit var mockSpan: Span

    private lateinit var awsRumSpanApiImpl: AwsRumSpanApiImpl

    @BeforeEach
    fun setup() {
        every { mockOpenTelemetryAgent.getOpenTelemetry() } returns mockOpenTelemetry
        every { mockOpenTelemetry.getTracer(any()) } returns mockTracer
        every { mockTracer.spanBuilder(any()) } returns mockSpanBuilder
        every { mockSpanBuilder.startSpan() } returns mockSpan
        every { mockSpanBuilder.setAttribute(any(), any<Any>()) } returns mockSpanBuilder
        every { mockSpanBuilder.setSpanKind(any()) } returns mockSpanBuilder
        every { mockSpanBuilder.setStartTimestamp(any(), any()) } returns mockSpanBuilder
        every { mockSpan.storeInContext(any()) } returns mockk()

        awsRumSpanApiImpl = AwsRumSpanApiImpl(mockOpenTelemetryAgent)
    }

    @Test
    fun `test getTracer returns correct tracer`() {
        val customTracer = awsRumSpanApiImpl.getTracer("test-scope")

        verify {
            mockOpenTelemetry.getTracer("test-scope")
        }
        assertEquals(mockTracer, customTracer)
    }

    @Test
    fun `test startSpan creates and starts a span with correct attributes`() {
        val attributes =
            mapOf(
                "string" to "value",
                "boolean" to true,
                "int" to 123,
                "long" to 123L,
                "double" to 123.45,
            )

        val span =
            awsRumSpanApiImpl.startSpan(
                name = "test-span",
                screenName = "TestScreen",
                attributes = attributes,
            )

        verify {
            mockTracer.spanBuilder("test-span")
            mockSpan.setAttribute("string", "value")
            mockSpan.setAttribute("boolean", true)
            mockSpan.setAttribute("int", 123L)
            mockSpan.setAttribute("long", 123L)
            mockSpan.setAttribute("double", 123.45)
            mockSpan.setAttribute(RumConstants.SCREEN_NAME_KEY, "TestScreen")
        }
        assertEquals(mockSpan, span)
    }

    @Test
    fun `test startSpan without screenName doesn't set screen attribute`() {
        awsRumSpanApiImpl.startSpan(
            name = "test-span",
            screenName = null,
            attributes = null,
        )

        verify(exactly = 0) {
            mockSpan.setAttribute(RumConstants.SCREEN_NAME_KEY, any<String>())
        }
    }

    @Test
    fun `test startSpan set span kind correctly`() {
        val spanWithDefault =
            awsRumSpanApiImpl.startSpan(
                name = "test-span",
                screenName = null,
                attributes = null,
            )
        verify {
            mockSpanBuilder.setSpanKind(SpanKind.CLIENT)
        }
        assertEquals(spanWithDefault, mockSpan)

        val spanWithCustomSpanKind =
            awsRumSpanApiImpl.startSpan(
                name = "test-span",
                screenName = null,
                attributes = null,
                spanKind = SpanKind.CONSUMER,
            )
        verify {
            mockSpanBuilder.setSpanKind(SpanKind.CONSUMER)
        }
        assertEquals(spanWithCustomSpanKind, mockSpan)
    }

    @Test
    fun `test startSpan handles null attributes`() {
        val span =
            awsRumSpanApiImpl.startSpan(
                name = "test-span",
                screenName = null,
                attributes = null,
            )

        verify {
            mockTracer.spanBuilder("test-span")
            mockSpanBuilder.startSpan()
        }
        assertEquals(mockSpan, span)
    }

    @Test
    fun `test startSpan ignores unsupported attribute types`() {
        val attributes =
            mapOf(
                "valid" to "string",
                "invalid" to Object(),
            )

        awsRumSpanApiImpl.startSpan(
            name = "test-span",
            screenName = null,
            attributes = attributes,
        )

        verify {
            mockSpan.setAttribute("valid", "string")
        }
        verify(exactly = 0) {
            mockSpan.setAttribute("invalid", any<String>())
        }
    }

    @Test
    fun `test startSpan set an explicit start timestamp correctly`() {
        val span =
            awsRumSpanApiImpl.startSpan(
                name = "test-span",
                screenName = null,
                startTimeMs = 1750316263019,
            )
        verify {
            mockSpanBuilder.setStartTimestamp(1750316263019 * 1000000, TimeUnit.NANOSECONDS)
        }
        assertEquals(mockSpan, span)
    }

    @Test
    fun `test startChildSpan sets parent context correctly`() {
        val mockParentSpan =
            mockk<Span>(relaxed = true) {
                every { storeInContext(any()) } returns mockk()
            }
        every { mockSpanBuilder.setParent(any()) } returns mockSpanBuilder

        val span =
            awsRumSpanApiImpl.startChildSpan(
                name = "child-span",
                parent = mockParentSpan,
                screenName = "TestScreen",
                attributes = null,
            )

        verify {
            mockParentSpan.storeInContext(any())
            mockSpanBuilder.setParent(any())
        }
        assertEquals(mockSpan, span)
    }

    @Test
    fun `test startFragmentTTFDSpan creates and starts a fragment TTFD span with correct attributes`() {
        val fragmentName = "TestFragment"
        val span = awsRumSpanApiImpl.startFragmentTTFDSpan(fragmentName)

        verify {
            mockOpenTelemetry.getTracer(Constants.TraceScope.AWS_RUM_CUSTOM_TRACER)
            mockSpanBuilder.setAttribute(Constants.FRAGMENT_NAME_KEY, fragmentName)
            mockSpan.setAttribute(RumConstants.SCREEN_NAME_KEY, fragmentName)
        }
        assertEquals(mockSpan, span)
    }

    @Test
    fun `test executeSpan creates and ends a span correctly and return code block`() {
        val codeBlock = "test result"

        val actualResult =
            awsRumSpanApiImpl.executeSpan(
                name = "test-span",
                screenName = "TestScreen",
            ) { span ->
                assertSame(mockSpan, span)
                codeBlock
            }

        verify {
            mockSpan.end()
        }
        assertEquals(codeBlock, actualResult)
    }

    @Test
    fun `executeSpan create a span with parameters correctly`() {
        val screenName = "TestScreen"
        val attributes =
            mapOf(
                "string" to "value",
                "boolean" to true,
                "int" to 123,
                "long" to 123L,
                "double" to 123.45,
            )

        awsRumSpanApiImpl.executeSpan(
            name = "test-span",
            screenName = screenName,
            parent = null,
            attributes = attributes,
            spanKind = SpanKind.CONSUMER,
        ) { span ->
            "result"
        }

        verify {
            mockSpanBuilder.setSpanKind(SpanKind.CONSUMER)
            mockSpan.setAttribute(RumConstants.SCREEN_NAME_KEY, "TestScreen")
            mockSpan.setAttribute("string", "value")
            mockSpan.setAttribute("boolean", true)
            mockSpan.setAttribute("int", 123L)
            mockSpan.setAttribute("long", 123L)
            mockSpan.setAttribute("double", 123.45)
            mockSpan.end()
        }
    }

    @Test
    fun `executeSpan creates child span when parent is provided`() {
        val mockParentSpan =
            mockk<Span>(relaxed = true) {
                every { storeInContext(any()) } returns mockk()
            }
        every { mockSpanBuilder.setParent(any()) } returns mockSpanBuilder

        val name = "child-span"

        awsRumSpanApiImpl.executeSpan(
            name = name,
            screenName = null,
            parent = mockParentSpan,
            attributes = null,
            spanKind = null,
        ) { span ->
            "result"
        }

        verify {
            mockParentSpan.storeInContext(any())
            mockSpanBuilder.setParent(any())
            mockSpan.end()
        }
    }

    @Test
    fun `executeSpan handles exceptions correctly`() {
        val exception = RuntimeException("Test error")

        val thrown =
            assertThrows(RuntimeException::class.java) {
                awsRumSpanApiImpl.executeSpan(
                    name = "test-span",
                    screenName = null,
                    parent = null,
                    attributes = null,
                    spanKind = null,
                ) { span ->
                    throw exception
                }
            }
        assertEquals("Test error", thrown.message)

        verifyOrder {
            mockSpan.setStatus(StatusCode.ERROR)
            mockSpan.end()
        }
    }
}
