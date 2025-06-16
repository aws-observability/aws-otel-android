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
package software.amazon.opentelemetry.android.api.internal

import io.opentelemetry.api.common.AttributeKey

internal object Constants {
    object TraceScope {
        const val DEFAULT = "software.amazon.opentelemetry.custom-spans"
    }

    object Reserved {
        const val TIME_TO_FIRST_DRAW = "TimeToFirstDraw"
        val FRAGMENT_NAME_KEY: AttributeKey<String> = AttributeKey.stringKey("fragment.name")
        val reserved =
            setOf(
                TIME_TO_FIRST_DRAW,
            )
    }
}
