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

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Logger
import software.amazon.opentelemetry.android.OpenTelemetryRumClient

/**
 * A convenient client for custom log events. This method will create a LogRecord event using the
 * default OpenTelemetryRumClient logger if none is specified
 */
fun OpenTelemetryRumClient.Companion.event(
    eventName: String,
    body: String = "",
    attributes: Attributes? = null,
    logger: Logger? = null,
) {
    val instance = this.getInstance()
    if (instance == null) {
        return
    }

    val logsProvider = logger ?: instance.defaultLogger
    val log =
        logsProvider
            .logRecordBuilder()
            .setEventName(eventName)
            .setBody(body)

    if (attributes != null) {
        log.setAllAttributes(attributes)
    }

    log.emit()
}
