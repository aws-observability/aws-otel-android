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
package software.amazon.opentelemetry.android.demo.simple

import android.app.Application
import kotlinx.coroutines.runBlocking
import software.amazon.opentelemetry.android.AwsRumAppMonitorConfig
import software.amazon.opentelemetry.android.OpenTelemetryAgent
import software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporter

class SimpleAwsDemoApplication : Application() {

    // Replace these with your actual AWS credentials and configuration
    private val cognitoPoolId = "us-east-1:<id>" // Replace with your Cognito Identity Pool ID
    private val awsRegion = "us-east-1" // Replace with your AWS region

    lateinit var awsService: AwsService

    override fun onCreate() {
        super.onCreate()

        awsService = AwsService(cognitoPoolId, awsRegion)

        // Initialize AWS OpenTelemetry Agent
        val appMonitorConfig = AwsRumAppMonitorConfig(
            region = "YOUR_REGION_FROM_CDK_OUTPUT",
            appMonitorId = "YOUR_APP_MONITOR_ID_FROM_CDK_OUTPUT"
        )

        // Default configuration - sends data to X-Ray only (for now)
        val otelAgent = OpenTelemetryAgent.Builder(this)
            .setAppMonitorConfig(appMonitorConfig)
            .addSpanExporterCustomizer {
                AwsSigV4SpanExporter.builder()
                    .setRegion(awsRegion)
                    .setEndpoint("https://xray.us-east-1.amazonaws.com/v1/traces")
                    .setServiceName("xray")
                    .setCredentialsProvider(awsService.cognitoCredentialsProvider)
                    .build()
            }
            .build()

        // For local development with OpenTelemetry Collector
        // Uncomment the following code and comment out the default configuration above
        /*
        // 10.0.2.2 is the special IP that Android emulator uses to communicate with the host
        // Replace with your actual IP if needed
        val localEndpoint = "http://10.0.2.2:4318"

        val otelAgent = OpenTelemetryAgent.Builder(this)
            .setAppMonitorConfig(appMonitorConfig)
            .setApplicationVersion("1.0.0")
            // Configure span exporter to use local collector
            .addSpanExporterCustomizer { _ ->
                OtlpHttpSpanExporter.builder()
                    .setEndpoint("$localEndpoint/v1/traces")
                    .build()
            }
            // Configure log record exporter to use local collector
            .addLogRecordExporterCustomizer { _ ->
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint("$localEndpoint/v1/logs")
                    .build()
            }
            .build()
        */
        
        // Alternative: Using gRPC exporters instead of HTTP
        /*
        val localGrpcEndpoint = "http://10.0.2.2:4317"
        
        val otelAgent = OpenTelemetryAgent.Builder(this)
            .setAppMonitorConfig(appMonitorConfig)
            .setApplicationVersion("1.0.0")
            // Configure span exporter to use local collector via gRPC
            .addSpanExporterCustomizer { _ ->
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint(localGrpcEndpoint)
                    .build()
            }
            // Configure log record exporter to use local collector via gRPC
            .addLogRecordExporterCustomizer { _ ->
                OtlpGrpcLogRecordExporter.builder()
                    .setEndpoint(localGrpcEndpoint)
                    .build()
            }
            .build()
        */
    }
}
