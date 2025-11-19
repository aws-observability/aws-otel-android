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
package software.amazon.opentelemetry.android.common

import io.opentelemetry.api.common.AttributeKey

object Constants {
    val FRAGMENT_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("fragment.name")

    const val CUSTOM_TRACER_NAME = "tracer.custom"

    const val CUSTOM_LOGGER_NAME = "logger.custom"

    const val TIME_TO_FIRST_DRAW_SPAN_NAME = "app.screen.time_to_first_draw"
}
