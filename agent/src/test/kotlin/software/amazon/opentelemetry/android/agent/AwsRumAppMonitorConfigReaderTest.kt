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

class AwsRumAppMonitorConfigReaderTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockResources: Resources

    private val validJson =
        """
        {
            "rum": {
                "appMonitorId": "testing",
                "region": "test-region",
                "alias": "my-alias",
                "sessionInactivityTimeout": 100,
                "overrideEndpoint":{
                    "logs":"http://test.com",
                    "traces":"http://test123.com"
                }
            },
            "application": {
                "applicationVersion":"1.0.0"
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
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.rum)
        assertEquals("testing", result!!.rum.appMonitorId)
        assertEquals("test-region", result.rum.region)
        assertEquals("my-alias", result.rum.alias)
    }

    @Test
    fun `test return null when no resources found`() {
        // Given
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(0)

        // When
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNull(result)
    }

    @Test
    fun `test error logging when invalid configuration provided`() {
        // Given
        val invalidJson =
            """
            {
                 "rum": {
                    "appMonitorId": "testing",
                    "region": "test-region"
                }
            }
            """.trimIndent()

        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(invalidJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNull(result)
        io.mockk.verify {
            Log.e(
                AwsRumAppMonitorConfigReader.TAG,
                "Missing fields in config: [application]",
            )
        }
    }

    @Test
    fun `test should return specified logs endpoint`() {
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(validJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.rum)
        assertEquals("http://test.com", AwsRumAppMonitorConfigReader.getLogsEndpoint(result!!))
    }

    @Test
    fun `test should return specified spans endpoint`() {
        `when`(mockResources.getIdentifier("aws_config", "string", "test.package"))
            .thenReturn(0)
        `when`(mockResources.getIdentifier("aws_config", "raw", "test.package"))
            .thenReturn(456)
        `when`(mockResources.openRawResource(456))
            .thenReturn(ByteArrayInputStream(validJson.toByteArray(StandardCharsets.UTF_8)))

        // When
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.rum)
        assertEquals("http://test123.com", AwsRumAppMonitorConfigReader.getTracesEndpoint(result!!))
    }

    @Test
    fun `test should return default rum endpoint when override not specified`() {
        val validJson =
            """
            {
                "rum": {
                    "appMonitorId": "testing",
                    "region": "test-region"
                },
                "application": {
                    "applicationVersion":"1.0.0"
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
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.rum)
        assertEquals(
            AwsRumAppMonitorConfigReader.buildRumEndpoint(result!!.rum.region),
            AwsRumAppMonitorConfigReader.getTracesEndpoint(result),
        )
    }

    @Test
    fun `test should return default sessionInactivityTimeout when not provided`() {
        val validJson =
            """
            {
                "rum": {
                    "appMonitorId": "testing",
                    "region": "test-region",
                    "alias": "my-alias"
                },
                "application": {
                    "applicationVersion":"1.0.0"
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
        val result = AwsRumAppMonitorConfigReader.readConfig(mockContext)

        // Then
        assertNotNull(result)
        assertNotNull(result?.rum)
        assertEquals(
            300,
            result?.rum?.sessionInactivityTimeout,
        )
    }
}
