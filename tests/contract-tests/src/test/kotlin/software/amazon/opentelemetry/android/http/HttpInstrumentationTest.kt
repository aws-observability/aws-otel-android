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
import software.amazon.opentelemetry.android.allAttributes
import software.amazon.opentelemetry.android.attributes
import software.amazon.opentelemetry.android.getAttributes
import software.amazon.opentelemetry.android.lacksAttribute
import software.amazon.opentelemetry.android.otlp.Span
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

        const val HTTP_200_URL = "http://10.0.2.2:8181/200"
        const val HTTP_404_URL = "http://10.0.2.2:8181/404"
        const val HTTP_500_URL = "http://10.0.2.2:8181/500"

        const val EXPECTED_REQUEST_HEADER = "request-header"
        const val IGNORED_REQUEST_HEADER = "ignored-request-header"
        const val EXPECTED_RESPONSE_HEADER = "response-header"
        const val IGNORED_RESPONSE_HEADER = "ignored-response-header"

        const val EXPECTED_HEADER_VALUE = "you should see me in telemetry" // same for both headers
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

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_200_URL
            },
        )

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_404_URL
            },
        )

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_500_URL
            },
        )

        Assertions.assertTrue(
            spans
                .filter { span: Span ->
                    span.getAttributes(URL_FULL).value.stringValue == HTTP_500_URL
                }.attributes(STATUS_CODE_ATTR)
                .value.intValue == "500",
        )
        Assertions.assertTrue(
            spans
                .filter { span: Span ->
                    span.getAttributes(URL_FULL).value.stringValue == HTTP_404_URL
                }.attributes(STATUS_CODE_ATTR)
                .value.intValue == "404",
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

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_200_URL
            },
        )

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_404_URL
            },
        )

        Assertions.assertNotNull(
            spans.attributes(SERVER_ADDR_ATTR).value.stringValue,
        )

        Assertions.assertNotNull(
            spans.attributes(SERVER_PORT_ATTR).value.intValue,
        )

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_404_URL
            },
        )

        Assertions.assertTrue(
            spans.any { span ->
                span.getAttributes(URL_FULL).value.stringValue == HTTP_500_URL
            },
        )

        Assertions.assertTrue(
            spans
                .filter { span: Span ->
                    span.getAttributes(URL_FULL).value.stringValue == HTTP_500_URL
                }.allAttributes(STATUS_CODE_ATTR)
                .all { attribute ->
                    attribute.value.intValue == "500"
                },
        )

        Assertions.assertTrue(
            spans
                .filter { span: Span ->
                    span.getAttributes(URL_FULL).value.stringValue == HTTP_404_URL
                }.allAttributes(STATUS_CODE_ATTR)
                .all { attribute ->
                    attribute.value.intValue == "404"
                },
        )
    }

    @Test
    fun `HTTP spans should all have the expected request and response headers AND no ignored headers`(data: ParsedOtlpData) {
        val okHttpScope = data.traces.scopeSpans(HTTP3_SCOPE)
        val okHttpSpans = okHttpScope.spans("GET")
        val urlConnectionScope = data.traces.scopeSpans(HTTP_URL_CONNECTION_SCOPE)
        val urlConnectionSpans = urlConnectionScope.spans("GET")

        val allSpans = okHttpSpans.union(urlConnectionSpans)
        Assertions.assertTrue(
            allSpans.all { span ->
                span
                    .getAttributes("http.request.header.${EXPECTED_REQUEST_HEADER}")
                    .value.arrayValue!!
                    .values
                    .first()
                    .stringValue ==
                    EXPECTED_HEADER_VALUE
            },
        )
        Assertions.assertTrue(
            allSpans.all { span ->
                span
                    .getAttributes("http.response.header.${EXPECTED_RESPONSE_HEADER}")
                    .value.arrayValue!!
                    .values
                    .first()
                    .stringValue ==
                    EXPECTED_HEADER_VALUE
            },
        )
        Assertions.assertTrue(
            allSpans.all { span ->
                span.lacksAttribute("http.request.header.${IGNORED_REQUEST_HEADER}")
            },
        )
        Assertions.assertTrue(
            allSpans.all { span ->
                span.lacksAttribute("http.response.header.${IGNORED_RESPONSE_HEADER}")
            },
        )
    }
}
