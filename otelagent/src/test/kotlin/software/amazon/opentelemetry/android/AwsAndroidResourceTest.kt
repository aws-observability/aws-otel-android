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
package software.amazon.opentelemetry.android

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import io.opentelemetry.android.BuildConfig
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes
import io.opentelemetry.semconv.incubating.OsIncubatingAttributes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class AwsAndroidResourceTest {
    private val appName = "awsTestApp"
    private val rumSdkVersion = BuildConfig.OTEL_ANDROID_VERSION
    private val osDescription =
        StringBuilder()
            .append("Android Version ")
            .append(Build.VERSION.RELEASE)
            .append(" (Build ")
            .append(Build.ID)
            .append(" API level ")
            .append(Build.VERSION.SDK_INT)
            .append(")")
            .toString()

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private val app: Application? = null

    @Test
    fun `should Create Correct Resource Typ eWith Region And Rum App Monitor Id`() {
        val appInfo = ApplicationInfo()
        appInfo.labelRes = 12345

        Mockito.`when`(app!!.applicationContext.applicationInfo).thenReturn(appInfo)
        Mockito.`when`(app.applicationContext.getString(appInfo.labelRes)).thenReturn(appName)

        val expected =
            Resource
                .getDefault()
                .merge(
                    Resource
                        .builder()
                        .put(ServiceAttributes.SERVICE_NAME, appName)
                        .put(RumConstants.RUM_SDK_VERSION, rumSdkVersion)
                        .put(
                            DeviceIncubatingAttributes.DEVICE_MODEL_NAME,
                            Build.MODEL,
                        ).put(
                            DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER,
                            Build.MODEL,
                        ).put(
                            DeviceIncubatingAttributes.DEVICE_MANUFACTURER,
                            Build.MANUFACTURER,
                        ).put(OsIncubatingAttributes.OS_NAME, "Android")
                        .put(OsIncubatingAttributes.OS_TYPE, "linux")
                        .put(
                            OsIncubatingAttributes.OS_VERSION,
                            Build.VERSION.RELEASE,
                        ).put(OsIncubatingAttributes.OS_DESCRIPTION, osDescription)
                        .put(
                            AwsRumConstants.RUM_APP_MONITOR_ID,
                            "test-app-monitor-id",
                        ).put(AwsRumConstants.AWS_REGION, "test-region")
                        .build(),
                )

        val result =
            AwsAndroidResource.createDefault(
                app,
                AwsRumAppMonitorConfig("test-region", "test-app-monitor-id"),
            )

        Assertions.assertEquals(expected, result)
    }
}
