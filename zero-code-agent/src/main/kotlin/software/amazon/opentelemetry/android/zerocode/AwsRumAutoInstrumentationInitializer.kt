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
package software.amazon.opentelemetry.android.zerocode

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import software.amazon.opentelemetry.android.OpenTelemetryAgent

internal class AwsRumAutoInstrumentationInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        // This will be called before Application.onCreate()

        val config = AwsRumAppMonitorConfigReader.readConfig(context!!)

        val application = context!!.applicationContext as Application

        if (config != null) {
            // Default configuration - sends data to AWS RUM
            val otelAgent =
                OpenTelemetryAgent
                    .Builder(application)
                    .setAppMonitorConfig(config.rum)
                    .setApplicationVersion("1.0.0")
                    .build()
        }

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
