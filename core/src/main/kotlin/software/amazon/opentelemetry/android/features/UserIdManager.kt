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

import android.app.Application
import android.content.Context
import io.opentelemetry.api.trace.TraceId
import java.util.Random

internal class UserIdManager(
    private val diskManager: DiskManager,
) : ModularFeature,
    AttributesProvidingFeature {
    companion object {
        const val USER_ID_FILE = "software.amazon.opentelemetry.android.userid"
        const val USER_ID_ATTR = "user.id"
    }

    private lateinit var application: Application
    private lateinit var context: Context

    var userId: String = ""

    override fun install(
        application: Application,
        context: Context,
    ) {
        this.application = application
        this.context = context

        val readUserId = diskManager.readFromFileIfExists(context, USER_ID_FILE)
        if (readUserId == null) {
            userId = generateUserId()
            diskManager.writeToFile(context, USER_ID_FILE, userId.toByteArray())
        } else {
            userId = readUserId
        }
    }

    override fun buildAttributes(): Map<String, String> {
        return if (userId.isNotBlank()) {
            mapOf(USER_ID_ATTR to userId)
        } else {
            return mapOf()
        }
    }

    private fun generateUserId(): String {
        val random = Random()
        return TraceId.fromLongs(random.nextLong(), random.nextLong())
    }
}
