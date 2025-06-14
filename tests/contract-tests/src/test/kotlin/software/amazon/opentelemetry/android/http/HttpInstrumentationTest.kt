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
package software.amazon.opentelemetry.android.http

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.opentelemetry.android.OtlpResolver
import software.amazon.opentelemetry.android.ParsedOtlpData
import software.amazon.opentelemetry.android.attributes
import software.amazon.opentelemetry.android.getAttributes
import software.amazon.opentelemetry.android.scopeSpans
import software.amazon.opentelemetry.android.spans

@ExtendWith(OtlpResolver::class)
class HttpInstrumentationTest {
    companion object {
        const val HTTP_URL_CONNECTION_SCOPE = "io.opentelemetry.android.http-url-connection"
        const val HTTP3_SCOPE = "io.opentelemetry.okhttp-3.0"
        const val HTTP_REQUEST_METHOD_ATTR = "http.request.method"
        const val STATUS_CODE_ATTR = "http.response.status_code"
        const val URL_FULL = "url.full"
        const val SERVER_ADDR_ATTR = "server.address"
        const val SERVER_PORT_ATTR = "server.port"
    }

    @Test
    fun `HTTP Spans from http-url-connection should have correct format`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(HTTP_URL_CONNECTION_SCOPE)
        val spans = scopeSpans.spans("GET")

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(HTTP_REQUEST_METHOD_ATTR).value.stringValue == "GET"
            },
        )

        Assertions.assertNotNull(
            spans.attributes(STATUS_CODE_ATTR).value.intValue,
        )

        Assertions.assertEquals(
            spans.attributes(URL_FULL).value.stringValue,
            "https://www.android.com",
        )
    }

    @Test
    fun `HTTP Spans from okhttp3 should have correct format`(data: ParsedOtlpData) {
        val scopeSpans = data.traces.scopeSpans(HTTP3_SCOPE)
        val spans = scopeSpans.spans("GET")

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(HTTP_REQUEST_METHOD_ATTR).value.stringValue == "GET"
            },
        )

        Assertions.assertNotNull(
            spans.attributes(STATUS_CODE_ATTR).value.intValue,
        )

        Assertions.assertEquals(
            spans.attributes(URL_FULL).value.stringValue,
            "https://www.android.com/",
        )

        Assertions.assertNotNull(
            spans.attributes(SERVER_ADDR_ATTR).value.stringValue,
        )

        Assertions.assertNotNull(
            spans.attributes(SERVER_PORT_ATTR).value.intValue,
        )
    }
}
