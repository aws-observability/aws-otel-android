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

import io.opentelemetry.context.Context
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.ReadWriteLogRecord
import io.opentelemetry.semconv.ExceptionAttributes

/**
 * A simple LogRecordProcessor that will fill exception.message with exception.type if there is no
 * exception.message in the span.
 */
class ExceptionMessageFillerLogRecordProcessor : LogRecordProcessor {
    // Fill exception.message only when not present
    override fun onEmit(
        context: Context,
        logRecord: ReadWriteLogRecord,
    ) {
        val exceptionMessage = logRecord.getAttribute(ExceptionAttributes.EXCEPTION_MESSAGE)
        if (exceptionMessage != null) {
            return
        }

        // Only fill if we have an exception.type
        val exceptionType = logRecord.getAttribute(ExceptionAttributes.EXCEPTION_TYPE) ?: return
        logRecord.setAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, exceptionType)
    }
}
