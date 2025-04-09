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
package software.amazon.opentelemetry.android;

import static io.opentelemetry.android.common.RumConstants.RUM_SDK_VERSION;
import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;
import static io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes.DEVICE_MANUFACTURER;
import static io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes.DEVICE_MODEL_IDENTIFIER;
import static io.opentelemetry.semconv.incubating.DeviceIncubatingAttributes.DEVICE_MODEL_NAME;
import static io.opentelemetry.semconv.incubating.OsIncubatingAttributes.OS_DESCRIPTION;
import static io.opentelemetry.semconv.incubating.OsIncubatingAttributes.OS_NAME;
import static io.opentelemetry.semconv.incubating.OsIncubatingAttributes.OS_TYPE;
import static io.opentelemetry.semconv.incubating.OsIncubatingAttributes.OS_VERSION;

import android.app.Application;
import android.os.Build;
import io.opentelemetry.android.BuildConfig;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.function.Supplier;

class AwsAndroidResource {
    static Resource createDefault(Application application, AwsRumAppMonitorConfig config) {
        String appName = readAppName(application);
        ResourceBuilder resourceBuilder =
                Resource.getDefault().toBuilder().put(SERVICE_NAME, appName);

        return resourceBuilder
                .put(RUM_SDK_VERSION, BuildConfig.OTEL_ANDROID_VERSION)
                .put(DEVICE_MODEL_NAME, Build.MODEL)
                .put(DEVICE_MODEL_IDENTIFIER, Build.MODEL)
                .put(DEVICE_MANUFACTURER, Build.MANUFACTURER)
                .put(OS_NAME, "Android")
                .put(OS_TYPE, "linux")
                .put(OS_VERSION, Build.VERSION.RELEASE)
                .put(OS_DESCRIPTION, getOSDescription())
                .put(AwsRumConstants.AWS_REGION, config.getRegion())
                .put(AwsRumConstants.RUM_APP_MONITOR_ID, config.getAppMonitorId())
                .build();
    }

    private static String readAppName(Application application) {
        return trapTo(
                () -> {
                    int stringId =
                            application.getApplicationContext().getApplicationInfo().labelRes;
                    return application.getApplicationContext().getString(stringId);
                },
                "unknown_service:android");
    }

    private static String trapTo(Supplier<String> fn, String defaultValue) {
        try {
            return fn.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String getOSDescription() {
        StringBuilder osDescriptionBuilder = new StringBuilder();
        return osDescriptionBuilder
                .append("Android Version ")
                .append(Build.VERSION.RELEASE)
                .append(" (Build ")
                .append(Build.ID)
                .append(" API level ")
                .append(Build.VERSION.SDK_INT)
                .append(")")
                .toString();
    }
}
