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
package software.amazon.opentelemetry.android.agent

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class AwsConfigReaderTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    private val validJson =
        """
        {
          "aws": {
            "region": "us-east-1",
            "rumAppMonitorId": "testing",
            "rumAlias": "my-alias"
          },
          "exportOverride": {
            "logs": "testlogs",
            "traces": "testtraces"
          },
          "telemetry": {
            "activity": { "enabled": true },
            "anr": { "enabled": true },
            "crash": { "enabled": true },
            "fragment": { "enabled": true },
            "network": { "enabled": true },
            "slowRendering": { "enabled": true },
            "startup": { "enabled": true },
            "httpUrlConnection": { "enabled": true },
            "okHttp3": { "enabled": true },
            "uiLoad": { "enabled": true }
          },
          "sessionTimeout": 100,
          "applicationAttributes": {
            "application.version": "1.0.0",
            "demo": true
          }
        }
        """.trimIndent()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockContext.packageName).thenReturn("test.package")
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun `test reading from raw resource successful`() {
        // Given
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(validJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.aws)
        assertNotNull(result?.exportOverride)
        assertNotNull(result?.telemetry)
        assertNotNull(result?.applicationAttributes)
        assertEquals("testing", result!!.aws.rumAppMonitorId)
        assertEquals("us-east-1", result.aws.region)
        assertEquals("my-alias", result.aws.rumAlias)
        assertEquals(true, result.telemetry!!.activity!!.enabled)
        assertEquals(true, result.telemetry!!.anr!!.enabled)
        assertEquals(true, result.telemetry!!.crash!!.enabled)
        assertEquals(true, result.telemetry!!.uiLoad!!.enabled)
        assertEquals(100, result.sessionTimeout)
        assertEquals("testlogs", result.exportOverride!!.logs)
        assertEquals("testtraces", result.exportOverride!!.traces)
    }

    @Test
    fun `test return null when no resources found`() {
        // Given
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(0)

        // When
        val result = AwsConfigReader.readConfig(mockContext)

        // Then
        assertNull(result)
    }

    @Test
    fun `test error logging when invalid configuration provided`() {
        // Given
        val invalidJson =
            """
            {
                 "telemetry": {}
            }
            """.trimIndent()

        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(invalidJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsConfigReader.readConfig(mockContext)

        // Then
        assertNull(result)
        io.mockk.verify {
            Log.e(
                AwsConfigReader.TAG,
                "Missing fields in config: [aws]",
            )
        }
    }

    @Test
    fun `test should return default rum endpoint when override not specified`() {
        val validJson =
            """
            {
                "aws": {
                    "region": "us-east-1",
                    "rumAppMonitorId": "testing",
                    "rumAlias": "my-alias"
                }
            }
            """.trimIndent()
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(validJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.aws)
        assertEquals(
            AwsConfigReader.buildRumEndpoint(result!!.aws.region),
            AwsConfigReader.getTracesEndpoint(result),
        )
    }

    @Test
    fun `test should return default sessionInactivityTimeout when not provided`() {
        val validJson =
            """
            {
                "aws": {
                    "region": "us-east-1",
                    "rumAppMonitorId": "testing",
                    "rumAlias": "my-alias"
                }
            }
            """.trimIndent()
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(validJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.aws)
        assertEquals(
            300,
            result?.sessionTimeout,
        )
    }
}
