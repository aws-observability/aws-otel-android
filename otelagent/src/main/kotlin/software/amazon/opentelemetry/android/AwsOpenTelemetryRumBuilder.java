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
package software.amazon.opentelemetry.android;

import static java.util.Objects.requireNonNull;

import android.app.Application;
import io.opentelemetry.android.OpenTelemetryRum;
import io.opentelemetry.android.OpenTelemetryRumBuilder;
import io.opentelemetry.android.instrumentation.AndroidInstrumentation;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.function.Function;

public class AwsOpenTelemetryRumBuilder {
    private OpenTelemetryRumBuilder openTelemetryRumBuilder;
    private AwsOtelRumConfig awsConfig;

    AwsOpenTelemetryRumBuilder(Application application, AwsOtelRumConfig config) {
        this.openTelemetryRumBuilder = OpenTelemetryRumBuilder.create(application, config);
        this.awsConfig = config;
    }

    public static AwsOpenTelemetryRumBuilder create(
            Application application, AwsOtelRumConfig config) {
        return new AwsOpenTelemetryRumBuilder(application, config);
    }

    public AwsOpenTelemetryRumBuilder addSpanExporterCustomizer(
            Function<? super SpanExporter, ? extends SpanExporter> spanExporterCustomizer) {
        requireNonNull(spanExporterCustomizer, "spanExporterCustomizer");
        this.openTelemetryRumBuilder.addSpanExporterCustomizer(spanExporterCustomizer);
        return this;
    }

    public AwsOpenTelemetryRumBuilder addLogRecordExporterCustomizer(
            Function<? super LogRecordExporter, ? extends LogRecordExporter>
                    logRecordExporterCustomizer) {
        this.openTelemetryRumBuilder.addLogRecordExporterCustomizer(logRecordExporterCustomizer);
        return this;
    }

    public AwsOpenTelemetryRumBuilder addInstrumentation(AndroidInstrumentation instrumentation) {
        this.openTelemetryRumBuilder.addInstrumentation(instrumentation);
        return this;
    }

    public OpenTelemetryRum build() {
        return this.openTelemetryRumBuilder.build();
    }
}
