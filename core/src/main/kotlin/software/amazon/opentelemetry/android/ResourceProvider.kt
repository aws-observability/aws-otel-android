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
import android.os.Build
import io.opentelemetry.android.common.RumConstants
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.semconv.ServiceAttributes
import io.opentelemetry.semconv.incubating.CloudIncubatingAttributes
import io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes
import io.opentelemetry.semconv.incubating.OsIncubatingAttributes
import java.util.function.Supplier

/**
 * An AWS-compatible resource provider for Android runtime
 */
object ResourceProvider {
    const val ANDROID_OS_NAME = "Android"
    const val ANDROID_OS_TYPE = "linux"
    const val AWS_RUM_CLOUD_PLATFORM = "aws_rum"
    const val AWS_CLOUD_PROVIDER = "aws"

    fun createDefault(
        application: Application,
        config: AwsRumAppMonitorConfig,
        appName: String? = null,
    ): Resource {
        val customApplicationName = appName ?: readAppName(application)

        val resourceBuilder =
            Resource
                .getDefault()
                .toBuilder()
                .put(ServiceAttributes.SERVICE_NAME, customApplicationName)
                .put(RumConstants.RUM_SDK_VERSION, BuildConfig.RUM_SDK_VERSION)
                .put(DeviceIncubatingAttributes.DEVICE_MODEL_NAME, Build.MODEL)
                .put(DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                .put(DeviceIncubatingAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                .put(OsIncubatingAttributes.OS_NAME, ANDROID_OS_NAME)
                .put(OsIncubatingAttributes.OS_TYPE, ANDROID_OS_TYPE)
                .put(OsIncubatingAttributes.OS_VERSION, Build.VERSION.RELEASE)
                .put(OsIncubatingAttributes.OS_BUILD_ID, Build.ID)
                .put(OsIncubatingAttributes.OS_DESCRIPTION, getOSDescription())
                .put(CloudIncubatingAttributes.CLOUD_PLATFORM, AWS_RUM_CLOUD_PLATFORM)
                .put(CloudIncubatingAttributes.CLOUD_PROVIDER, AWS_CLOUD_PROVIDER)
                .put(CloudIncubatingAttributes.CLOUD_REGION, config.region)
                .put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ID, config.appMonitorId)

        if (!config.alias.isNullOrEmpty()) {
            resourceBuilder.put(AwsRumAttributes.AWS_RUM_APP_MONITOR_ALIAS, config.alias)
        }

        return resourceBuilder.build()
    }

    private fun readAppName(application: Application): String =
        trapTo(
            {
                val stringId =
                    application.applicationContext.applicationInfo.labelRes
                application.applicationContext.getString(stringId)
            },
            "unknown_service:android",
        )

    private fun trapTo(
        fn: Supplier<String>,
        defaultValue: String,
    ): String =
        try {
            fn.get()
        } catch (e: Exception) {
            defaultValue
        }

    private fun getOSDescription(): String {
        val osDescriptionBuilder = StringBuilder()
        return osDescriptionBuilder
            .append("Android Version ")
            .append(Build.VERSION.RELEASE)
            .append(" (Build ")
            .append(Build.ID)
            .append(" API level ")
            .append(Build.VERSION.SDK_INT)
            .append(")")
            .toString()
    }
}
