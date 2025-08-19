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
package software.amazon.opentelemetry.android.processor

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor

/**
 * This SpanProcessor uses the experimental ExtendedSpanProcessor API to append OS process cpu
 * statistics into span attributes. We establish 'cpu utilization average' to be:
 *
 *      cpuUtilizationAvg = 100 * (cpuTimeMs / spanDurationMs) / number of CPU cores
 *          * cpuTimeMs is the time in milliseconds that the app process has taken in active CPU
 *            time
 *          * spanDurationMs is the total running time in milliseconds that the span has been active for
 *
 * The processor appends the following attributes:
 *  * [process.cpu.avg_utilization] - The relative, average CPU utilization for the app process during the span
 *  * [process.cpu_elapsed_time_start_millis] - The elapsed CPU time at the start of this span
 *  * [process.cpu_elapsed_time_end_millis] - The elapsed CPU time at the end of this span
 */
class CpuAttributesSpanProcessor(
    private val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
) : ExtendedSpanProcessor {
    companion object {
        const val CPU_AVERAGE_ATTRIBUTE = "process.cpu.avg_utilization"
        const val CPU_ELAPSED_TIME_START = "process.cpu.elapsed_time_start_millis"
        const val CPU_ELAPSED_TIME_END = "process.cpu.elapsed_time_end_millis"
    }

    override fun onStart(
        parentContext: Context,
        span: ReadWriteSpan,
    ) {
        span.setAttribute(CPU_ELAPSED_TIME_START, android.os.Process.getElapsedCpuTime())
    }

    override fun isStartRequired(): Boolean = true

    override fun onEnd(span: ReadableSpan) {}

    override fun isEndRequired(): Boolean = false

    override fun onEnding(span: ReadWriteSpan) {
        val startCpuTime =
            span.getAttribute(AttributeKey.longKey(CPU_ELAPSED_TIME_START))
                ?: return
        val endCpuTime = android.os.Process.getElapsedCpuTime()
        val cpuTimeMs = (endCpuTime - startCpuTime).toDouble()
        val spanDurationMs = (span.latencyNanos / 1_000_000).toDouble()

        if (spanDurationMs > 0) {
            val cpuUtilization = (cpuTimeMs / spanDurationMs) * 100.0 / cpuCores.toDouble()
            span.setAttribute(CPU_AVERAGE_ATTRIBUTE, cpuUtilization)
        }
        span.setAttribute(CPU_ELAPSED_TIME_END, endCpuTime)
    }

    override fun isOnEndingRequired(): Boolean = true
}
