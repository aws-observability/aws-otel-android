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
package software.amazon.opentelemetry.android.uiload.activity

import android.app.Application
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.verify
import io.opentelemetry.android.instrumentation.InstallationContext
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.opentelemetry.android.uiload.utils.CommonUtils
import software.amazon.opentelemetry.android.uiload.utils.CommonUtils.getVersionSDKInt

class ActivityLoadInstrumentationTest {
    @MockK
    private lateinit var installationContext: InstallationContext

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var openTelemetry: OpenTelemetry

    @MockK
    private lateinit var tracer: Tracer

    private lateinit var activityLoadInstrumentation: ActivityLoadInstrumentation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { installationContext.application } returns application
        every { installationContext.openTelemetry } returns openTelemetry
        every { openTelemetry.getTracer(any()) } returns tracer
        every { application.registerActivityLifecycleCallbacks(any()) } just runs

        activityLoadInstrumentation = ActivityLoadInstrumentation()
    }

    @Test
    fun `test installation with SDK version 29 or higher`() {
        mockkObject(CommonUtils)
        every { getVersionSDKInt() } returns 29

        activityLoadInstrumentation.install(installationContext)

        verify {
            openTelemetry.getTracer(ActivityLoadInstrumentation.INSTRUMENTATION_SCOPE)
            application.registerActivityLifecycleCallbacks(any<ActivityLoadCallback>())
        }
    }

    @Test
    fun `test installation with SDK version below 29`() {
        mockkObject(CommonUtils)
        every { getVersionSDKInt() } returns 28

        activityLoadInstrumentation.install(installationContext)

        verify {
            openTelemetry.getTracer(ActivityLoadInstrumentation.INSTRUMENTATION_SCOPE)
            application.registerActivityLifecycleCallbacks(any<Pre29ActivityLoadCallback>())
        }
    }

    @Test
    fun `test correct tracer initialization`() {
        activityLoadInstrumentation.install(installationContext)

        verify {
            openTelemetry.getTracer(ActivityLoadInstrumentation.INSTRUMENTATION_SCOPE)
        }
    }
}
