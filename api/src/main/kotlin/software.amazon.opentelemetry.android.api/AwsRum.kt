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
package software.amazon.opentelemetry.android.api

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import software.amazon.opentelemetry.android.api.internal.AwsRumImpl
import software.amazon.opentelemetry.android.api.internal.AwsRumSdkApi

class AwsRum private constructor(
    private val impl: AwsRumImpl = AwsRumImpl(),
) : AwsRumSdkApi {
    companion object {
        private val instance: AwsRum = AwsRum()

        @JvmStatic
        fun getInstance() = instance
    }

    override fun getTracer(instrumentationScope: String): Tracer = impl.getTracer(instrumentationScope)

    override fun startSpan(
        name: String,
        screenName: String?,
        attributes: Map<String, Any>?,
    ): Span = impl.startSpan(name, screenName, attributes)

    override fun startChildSpan(
        name: String,
        parent: Span,
        screenName: String?,
        attributes: Map<String, Any>?,
    ): Span = impl.startChildSpan(name, parent, screenName, attributes)

    override fun startFragmentTTFDSpan(fragmentName: String): Span = impl.startFragmentTTFDSpan(fragmentName)
}
