# AWS OpenTelemetry Android - Simple AWS Demo

This is a simple demo application that demonstrates how to use the AWS OpenTelemetry Android SDK to instrument AWS API calls.

## Features

- Integration with AWS OpenTelemetry Android SDK
- AWS S3 bucket listing
- AWS Cognito identity retrieval
- Automatic tracing of AWS API calls

## Infrastructure Setup

This demo includes a CDK project that creates all the necessary AWS infrastructure:

1. Navigate to the CDK directory:
   ```
   cd cdk
   ```

2. Deploy infrastructure using the script provided:
   ```
   ./demo-cdk.sh deploy
   ```

3. Note the outputs from the deployment, which you'll need to update in your Android app:
   - IdentityPoolId
   - AppMonitorId
   - Region

For more details, see the [CDK README](./cdk/README.md).

## App Setup

1. Replace the placeholder values in `SimpleAwsDemoApplication.kt` with your actual AWS RUM AppMonitor details:
   ```kotlin
   val appMonitorConfig = AwsRumAppMonitorConfig(
       region = "YOUR_REGION_FROM_CDK_OUTPUT",
       appMonitorId = "YOUR_APP_MONITOR_ID_FROM_CDK_OUTPUT"
   )
   ```

2. Replace the placeholder values in `MainActivity.kt` with your actual AWS credentials:
   ```kotlin
   private val cognitoPoolId = "YOUR_IDENTITY_POOL_ID_FROM_CDK_OUTPUT"
   private val awsRegion = Regions.YOUR_REGION_FROM_CDK_OUTPUT
   ```

## Local OpenTelemetry Collector Setup

For local development and testing, you can use the included OpenTelemetry Collector to view telemetry data:

1. Make sure you have Docker and Docker Compose installed on your system.

2. Create an output directory for the collector logs:
   ```bash
   mkdir -p out
   ```

3. Start the OpenTelemetry Collector using Docker Compose:
   ```bash
   docker-compose up
   # or with newer Docker versions:
   docker compose up
   ```

4. The collector will be available at:
   - OTLP gRPC: localhost:4317
   - OTLP HTTP: localhost:4318

5. Telemetry data will be written to the following files:
   - Traces: `./out/traces.txt`
   - Logs: `./out/logs.txt`

6. To view the telemetry data in real-time:
   ```bash
   # For traces
   tail -f out/traces.txt
   
   # For logs
   tail -f out/logs.txt
   ```

7. To stop the collector:
   ```bash
   docker-compose down
   # or with newer Docker versions:
   docker compose down
   ```

8. To use the local collector in your app, modify the OpenTelemetry configuration to point to your local collector endpoint instead of AWS services.

## How It Works

1. The application initializes the AWS OpenTelemetry Agent in the `SimpleAwsDemoApplication` class.
2. When you click on the "List S3 Buckets" button, the app makes an AWS S3 API call to list your buckets.
3. When you click on the "Get Cognito Identity" button, the app retrieves your Cognito identity ID.

## Requirements

- AWS CDK v2
- AWS account with appropriate permissions to deploy the CDK stack
- Docker and Docker Compose (for local collector setup)
