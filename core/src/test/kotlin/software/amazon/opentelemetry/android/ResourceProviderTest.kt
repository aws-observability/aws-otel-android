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
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.incubating.CloudIncubatingAttributes
import io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes
import io.opentelemetry.semconv.incubating.OsIncubatingAttributes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ResourceProviderTest {
    private val appName = "awsTestApp"
    private val rumSdkVersion = BuildConfig.RUM_SDK_VERSION
    private val osDescription = "Android Version ${Build.VERSION.RELEASE} (Build ${Build.ID} API level ${Build.VERSION.SDK_INT})"

    @RelaxedMockK
    lateinit var application: Application

    @Test
    fun `should create correct resource type with no custom resource provided`() {
        val appInfo = ApplicationInfo()
        appInfo.labelRes = 12345

        every { application.applicationContext.applicationInfo } returns appInfo
        every { application.applicationContext.getString(appInfo.labelRes) } returns appName

        val expected =
            Resource
                .getDefault()
                .merge(
                    Resource
                        .builder()
                        .put(ServiceAttributes.SERVICE_NAME, appName)
                        .put(RumConstants.RUM_SDK_VERSION, rumSdkVersion)
                        .put(DeviceIncubatingAttributes.DEVICE_MODEL_NAME, Build.MODEL)
                        .put(DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                        .put(DeviceIncubatingAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                        .put(OsIncubatingAttributes.OS_NAME, "Android")
                        .put(OsIncubatingAttributes.OS_TYPE, "linux")
                        .put(OsIncubatingAttributes.OS_VERSION, Build.VERSION.RELEASE)
                        .put(OsIncubatingAttributes.OS_BUILD_ID, Build.ID)
                        .put(OsIncubatingAttributes.OS_DESCRIPTION, osDescription)
                        .put(CloudIncubatingAttributes.CLOUD_PLATFORM, "aws_rum")
                        .put(CloudIncubatingAttributes.CLOUD_PROVIDER, "aws")
                        .put(CloudIncubatingAttributes.CLOUD_REGION, "test-region")
                        .put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ID, "test-app-monitor-id")
                        .put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ALIAS, "alias")
                        .build(),
                )

        val result =
            ResourceProvider.createDefault(
                application,
                AwsRumAppMonitorConfig("test-region", "test-app-monitor-id", "alias"),
                null,
            )

        Assertions.assertEquals(expected, result)
    }

    @Test
    fun `should do the correct merge when given a custom resource`() {
        val customResource =
            Resource
                .builder()
                .put(ServiceAttributes.SERVICE_NAME, "some-app-name")
                .put(ServiceAttributes.SERVICE_VERSION, "1.0")
                .put("deployment.environment", "staging")
                .build()

        val expected =
            Resource
                .getDefault()
                .merge(
                    Resource
                        .builder()
                        .put(ServiceAttributes.SERVICE_NAME, "some-app-name")
                        .put(ServiceAttributes.SERVICE_VERSION, "1.0")
                        .put(RumConstants.RUM_SDK_VERSION, rumSdkVersion)
                        .put(DeviceIncubatingAttributes.DEVICE_MODEL_NAME, Build.MODEL)
                        .put(DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                        .put(DeviceIncubatingAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                        .put(OsIncubatingAttributes.OS_NAME, "Android")
                        .put(OsIncubatingAttributes.OS_TYPE, "linux")
                        .put(OsIncubatingAttributes.OS_VERSION, Build.VERSION.RELEASE)
                        .put(OsIncubatingAttributes.OS_BUILD_ID, Build.ID)
                        .put(OsIncubatingAttributes.OS_DESCRIPTION, osDescription)
                        .put(CloudIncubatingAttributes.CLOUD_PLATFORM, "aws_rum")
                        .put(CloudIncubatingAttributes.CLOUD_PROVIDER, "aws")
                        .put(CloudIncubatingAttributes.CLOUD_REGION, "test-region")
                        .put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ID, "test-app-monitor-id")
                        .put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ALIAS, "alias")
                        .put("deployment.environment", "staging")
                        .build(),
                )

        val result =
            ResourceProvider.createDefault(
                application,
                AwsRumAppMonitorConfig("test-region", "test-app-monitor-id", "alias"),
                customResource,
            )

        Assertions.assertEquals(expected, result)
    }
}
