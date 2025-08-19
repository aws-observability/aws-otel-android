# ADOT Android Agent

The ADOT Android Agent provides (nearly) zero-code instrumentation for Android applications, automatically collecting telemetry data and exporting it to AWS CloudWatch RUM without requiring manual SDK initialization.

In short, all you need to do is:
1. Create an `aws_config.json` file to point your telemetry to your CloudWatch Real User Monitoring [app monitor](https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/aws-resource-rum-appmonitor.html).
2. Include the `agent` dependency in your Android application
3. (Optional) Include extra ByteBuddy dependencies for additional automated instrumentation

## What it does

The agent automatically instruments your Android application to collect all supported telemetry and export it to the configured destination via [OTLP spec](https://opentelemetry.io/docs/specs/otlp/).

## Quick Start

### 1. Add Dependencies

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:agent:${LATEST_VERSION}")

    // For HTTP instrumentation with ByteBuddy
    byteBuddy("io.opentelemetry.android.instrumentation:okhttp3-agent:0.12.0-alpha")           // if you are using OkHttp-3.0
    byteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:0.12.0-alpha") // if you are using URLConnection / HttpURLConnection / HttpsURLConnection
}
```

### 2. Create a configuration file

Create `res/raw/aws_config.json`:

```jsonc
{
  "aws": {
    // REQUIRED fields:
    "region": "us-east-1", // specify the AWS region your app monitor has been created in
    "rumAppMonitorId": "<your-app-monitor-id>",

    // OPTIONAL fields:
    "cognitoIdentityPoolId": "<cognito-identity-pool-id>" // for enhanced auth with Cognito, if your use case necessitates its use
  },
  
  // optional attributes that will be appended to all OpenTelemetry application spans and events
  "applicationAttributes": {
    "application.version": "1.0.0" // specifying application.version will allow you to filter telemetry on the RUM console based on your running app's version
  }
}
```

### 3. That's it!

No code changes required. The agent automatically initializes when your app starts and begins collecting telemetry.

Note that, in order to collect HTTP telemetry, you will need to depend upon the ByteBuddy instrumentation agent and include that in your build script.

## How it Works

The agent uses Android's `ContentProvider` initialization mechanism to automatically start before your `Application.onCreate()` method runs:

1. **Automatic Initialization**: `AwsRumAutoInstrumentationInitializer` runs at app startup
2. **Configuration Loading**: Reads `aws_config.json` from your app's resources
3. **OpenTelemetry Setup**: Configures the OpenTelemetry SDK with AWS-specific settings
4. **Instrumentation Registration**: Enables automatic telemetry collection
5. **Export Configuration**: Sets up OTLP exporters pointing to AWS RUM endpoints

## Configuration Options

| Option                                    | Type         | Required | Description                                                                                   | Example                                          |
|-------------------------------------------|--------------|----------|-----------------------------------------------------------------------------------------------|--------------------------------------------------|
| aws.region                                | string       | **Yes**  | AWS region to export telemetry to                                                             | "us-east-1"                                      |
| aws.rumAppMonitorId                       | string       | **Yes**  | AWS RUM Application Monitor ID                                                                | "00000000-0000-0000-0000-000000000000"           |
| aws.rumAlias                              | string       | No       | Alias for requests, used with resource-based policies                                         | "my-app-alias"                                   |
| aws.cognitoIdentityPoolId                 | string       | No       | AWS Cognito Identity Pool ID for authentication                                               | "us-east-1:a1b2c3d4-5678-90ab-cdef-EXAMPLE11111" |
| exportOverride.logs                       | string (URI) | No       | Override export destination for logs                                                          | "https://custom-endpoint.com/logs"               |
| exportOverride.traces                     | string (URI) | No       | Override export destination for traces                                                        | "https://custom-endpoint.com/traces"             |
| telemetry.activity.enabled                | boolean      | No       | Enable/disable activity lifecycle monitoring                                                  | true                                             |
| telemetry.anr.enabled                     | boolean      | No       | Enable/disable ANR detection                                                                  | true                                             |
| telemetry.crash.enabled                   | boolean      | No       | Enable/disable crash reporting                                                                | true                                             |
| telemetry.fragment.enabled                | boolean      | No       | Enable/disable fragment lifecycle monitoring                                                  | true                                             |
| telemetry.network.enabled                 | boolean      | No       | Enable/disable network state monitoring                                                       | true                                             |
| telemetry.slow_rendering.enabled          | boolean      | No       | Enable/disable slow UI rendering detection                                                    | true                                             |
| telemetry.startup.enabled                 | boolean      | No       | Enable/disable application startup monitoring                                                 | true                                             |
| telemetry.http_urlconnection.enabled      | boolean      | No       | Enable/disable HttpURLConnection monitoring                                                   | true                                             |
| telemetry.okhttp_3.0.enabled              | boolean      | No       | Enable/disable OkHttp 3.0 monitoring                                                          | true                                             |
| telemetry.ui_load.enabled                 | boolean      | No       | Enable/disable UI load time monitoring                                                        | true                                             |
| telemetry.session_events.enabled          | boolean      | No       | Enable/disable session event instrumentation                                                  | true                                             |
| sessionTimeout                            | integer      | No       | Max session inactivity duration in seconds (default: 300)                                     | 600                                              |
| sessionSampleRate                         | number       | No       | Proportion of sessions to record (0-1, default: 1)                                            | 0.5                                              |
| applicationAttributes                     | object       | No       | Custom application attributes added to all spans and logs                                     | {"version": "1.2.3", "environment": "prod"}      |
| applicationAttributes.application.version | string       | No       | A special application attribute you can add to filter on deployed app versions in RUM console | "1.0.0"                                          |

For the full JSON schema, please refer to [client-config/schema_v1.json](client-config/schema_v1.json)

## Telemetry Types

| Type             | Description                          |
|------------------|--------------------------------------|
| `activity`       | Activity lifecycle events            |
| `fragment`       | Fragment lifecycle events            |
| `network`        | HTTP request monitoring              |
| `crash`          | Crash detection and reporting        |
| `anr`            | Application Not Responding detection |
| `ui_loading`     | UI loading performance               |
| `slow_rendering` | Slow rendering detection             |
| `startup`        | App startup performance              |
| `http_urlconnection` | HTTP telemetry for HttpUrlConnection client |
| `okhttp_3.0`     | HTTP telemetry for OkHttp 3.0 client |

## Troubleshooting

### No Telemetry Generated

1. Verify `aws_config.json` exists in `res/raw/`
2. Check that required fields (`aws.region`, `aws.rumAppMonitorId`) are present
3. Ensure your RUM App Monitor exists and is active
4. Check your auth, if enabled, is correct and writing to the RUM Data Plane with 200 OK responses.
5. Check device network connectivity

### Configuration Errors

The agent logs configuration errors to Android's system log:

```bash
adb logcat | grep "AWS Otel Android"
```

Common errors:
- Missing required fields
- Invalid JSON syntax
- Invalid App Monitor ID format
