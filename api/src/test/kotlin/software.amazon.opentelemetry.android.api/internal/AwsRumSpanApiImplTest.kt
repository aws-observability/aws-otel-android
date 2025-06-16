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
package software.amazon.opentelemetry.android.api.internal

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.opentelemetry.android.common.RumConstants.SCREEN_NAME_KEY
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.api.internal.Constants.Reserved.FRAGMENT_NAME_KEY
import software.amazon.opentelemetry.android.uiload.activity.ActivityLoadInstrumentation.Companion.INSTRUMENTATION_SCOPE

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
        every { mockSpan.storeInContext(any()) } returns mockk()

        awsRumSpanApiImpl = AwsRumSpanApiImpl(mockOpenTelemetryAgent)
    }

    @Test
    fun `test getTracer returns correct tracer`() {
        val customTracer = awsRumSpanApiImpl.getTracer("test-scope")
        val defaultTracer = awsRumSpanApiImpl.getTracer()

        verify {
            mockOpenTelemetry.getTracer(Constants.TraceScope.DEFAULT)
        }
        assertEquals(mockTracer, defaultTracer)

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
            mockSpan.setAttribute(SCREEN_NAME_KEY, "TestScreen")
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
            mockSpan.setAttribute(SCREEN_NAME_KEY, any<String>())
        }
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
            mockOpenTelemetry.getTracer(INSTRUMENTATION_SCOPE)
            mockSpanBuilder.setAttribute(FRAGMENT_NAME_KEY, fragmentName)
            mockSpan.setAttribute(SCREEN_NAME_KEY, fragmentName)
        }
        assertEquals(mockSpan, span)
    }
}
