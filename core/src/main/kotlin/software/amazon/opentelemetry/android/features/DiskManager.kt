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
package software.amazon.opentelemetry.android.features

import android.content.Context
import android.util.Log
import java.io.FileNotFoundException

internal class DiskManager {
    companion object {
        @Volatile
        private var instance: DiskManager? = null

        @Synchronized
        fun getInstance(): DiskManager = instance ?: DiskManager().also { instance = it }
    }

    fun readFromFileIfExists(
        context: Context,
        fileName: String,
    ): String? =
        try {
            context.openFileInput(fileName).use {
                it.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: FileNotFoundException) {
            null
        }

    fun writeToFile(
        context: Context,
        fileName: String,
        contents: ByteArray,
    ): Boolean =
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(contents)
            }
            true
        } catch (e: Exception) {
            Log.w("Failed to write to file ($fileName): ", e)
            false
        }
}
