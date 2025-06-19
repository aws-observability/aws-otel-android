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

import software.amazon.opentelemetry.android.features.DiskManager
import software.amazon.opentelemetry.android.features.ModularFeature
import software.amazon.opentelemetry.android.features.UserIdManager

/**
 * An enum that allows selective enabling of client telemetry features
 */
enum class FeatureConfig(
    val configFlag: String,
    val feature: ModularFeature? = null,
) {
    /**
     * Enables generating and collecting the "user.id" telemetry attribute.
     *
     * This feature requires writing "user.id" to app-specific persistent file storage
     */
    USER_ID("attribute:user.id", UserIdManager(DiskManager.getInstance())),
    ;

    companion object {
        fun getDefault(): List<FeatureConfig> =
            listOf(
                USER_ID,
            )

        fun mapConfigFlag(flag: String): FeatureConfig? = values().find { it.configFlag == flag }
    }
}
