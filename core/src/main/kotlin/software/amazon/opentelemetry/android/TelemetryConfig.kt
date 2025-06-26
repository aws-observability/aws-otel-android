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

import io.opentelemetry.android.instrumentation.AndroidInstrumentation
import io.opentelemetry.android.instrumentation.activity.ActivityLifecycleInstrumentation
import io.opentelemetry.android.instrumentation.anr.AnrInstrumentation
import io.opentelemetry.android.instrumentation.crash.CrashReporterInstrumentation
import io.opentelemetry.android.instrumentation.fragment.FragmentLifecycleInstrumentation
import io.opentelemetry.android.instrumentation.network.NetworkChangeInstrumentation
import io.opentelemetry.android.instrumentation.slowrendering.SlowRenderingInstrumentation
import io.opentelemetry.android.instrumentation.startup.StartupInstrumentation
import io.opentelemetry.instrumentation.library.httpurlconnection.HttpUrlInstrumentation
import io.opentelemetry.instrumentation.library.okhttp.v3_0.OkHttpInstrumentation
import software.amazon.opentelemetry.android.uiload.activity.ActivityLoadInstrumentation

/**
 * An enum that allows selective enabling of telemetry
 */
enum class TelemetryConfig(
    val configFlag: String,
    val instrumentation: AndroidInstrumentation? = null,
) {
    /**
     * Enables SDK initialization telemetry upon building the Otel client
     */
    SDK_INITIALIZATION_EVENTS("sdk_initialization"),

    /**
     * Enables telemetry for Activity lifecycle monitoring
     */
    ACTIVITY("activity", ActivityLifecycleInstrumentation()),

    /**
     * Enables telemetry for Android "Application Not Responding" error monitoring
     */
    ANR("anr", AnrInstrumentation()),

    /**
     * Enables telemetry for Android crash reporting
     */
    CRASH("crash", CrashReporterInstrumentation()),

    /**
     * Enables telemetry for Fragment lifecycle monitoring
     */
    FRAGMENT("fragment", FragmentLifecycleInstrumentation()),

    /**
     * Enables telemetry for Android network event monitoring
     */
    NETWORK("network", NetworkChangeInstrumentation()),

    /**
     * Enables telemetry for Android UI slow rendering reports
     */
    SLOW_RENDERING("slow_rendering", SlowRenderingInstrumentation()),

    /**
     * Enables telemetry for Android application startup monitoring
     */
    STARTUP("startup", StartupInstrumentation()),

    /**
     * Enables telemetry for URLConnection / HttpURLConnection / HttpsURLConnection monitoring
     */
    HTTP_URLCONNECTION("http_urlconnection", HttpUrlInstrumentation()),

    /**
     * Enables telemetry for OkHttp version 3.0 or higher
     */
    OKHTTP_3("okhttp_3.0", OkHttpInstrumentation()),

    /**
     * Enables telemetry for UI load monitoring
     */
    UI_LOADING("ui_loading", ActivityLoadInstrumentation()),
    ;

    companion object {
        fun getDefault(): List<TelemetryConfig> =
            listOf(
                ACTIVITY,
                ANR,
                CRASH,
                FRAGMENT,
                NETWORK,
                SLOW_RENDERING,
                STARTUP,
                HTTP_URLCONNECTION,
                OKHTTP_3,
                UI_LOADING,
            )

        fun mapConfigFlag(flag: String): TelemetryConfig? = values().find { it.configFlag == flag }
    }
}
