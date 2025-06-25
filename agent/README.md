# ADOT Android Agent

The ADOT Android Agent provides (nearly) zero-code instrumentation for Android applications, automatically collecting telemetry data and exporting it to AWS CloudWatch RUM without requiring manual SDK initialization.

In short, all you need to do is:
1. Create an `aws_config.json` file to point your telemetry to your CloudWatch Real User Monitoring [AppMonitor](https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/aws-resource-rum-appmonitor.html).
2. Include the `agent` dependency in your Android application
3. (Optional) Include extra ByteBuddy dependencies for additional automated instrumentation 

## What it does

The agent automatically instruments your Android application to collect all supported telemetry and export it to the configured destination via [OTLP spec](https://opentelemetry.io/docs/specs/otlp/).

## Quick Start

### 1. Add Dependency

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:agent:LATEST_VERSION")
}
```

### 2. Create Configuration

Create `res/raw/aws_config.json`:

```json
{
  "rum": {
    "region": "us-east-1",
    "appMonitorId": "your-app-monitor-id",
    "alias": "your-app-alias"
  },
  "application": {
    "applicationVersion": "1.0.0"
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

### Required Fields

- `region`: AWS region where your RUM App Monitor is located
- `appMonitorId`: Your RUM App Monitor ID (UUID format)

### Optional Fields

- `alias`: Human-readable name for your app
- `sessionInactivityTimeout`: Session timeout in seconds (default: 300)
- `enabledTelemetry`: Array of telemetry types to collect
- `addonFeatures`: Additional features like user ID tracking

### Example with All Options

```json
{
  "rum": {
    "region": "us-east-1",
    "appMonitorId": "12345678-1234-1234-1234-123456789012",
    "alias": "MyMobileApp",
    "sessionInactivityTimeout": 600,
    "enabledTelemetry": [
      "activity",
      "fragment", 
      "network",
      "crash",
      "anr",
      "ui_load"
    ],
    "addonFeatures": [
      "attribute:user.id"
    ],
    "overrideEndpoint": {
      "traces": "https://custom-endpoint.com/v1/traces",
      "logs": "https://custom-endpoint.com/v1/logs"
    }
  },
  "application": {
    "applicationVersion": "2.1.0"
  }
}
```

## Telemetry Types

| Type | Description |
|------|-------------|
| `activity` | Activity lifecycle events |
| `fragment` | Fragment lifecycle events |
| `network` | HTTP request monitoring |
| `crash` | Crash detection and reporting |
| `anr` | Application Not Responding detection |
| `ui_load` | UI loading performance |
| `slow_rendering` | Slow rendering detection |
| `startup` | App startup performance |

## Architecture

```
┌─────────────────────────────────────────┐
│           Your Android App              │
├─────────────────────────────────────────┤
│  AwsRumAutoInstrumentationInitializer   │ ← ContentProvider (auto-runs)
│  ├── AwsRumAppMonitorConfigReader       │ ← Reads aws_config.json
│  └── OpenTelemetryAgent.Builder         │ ← Configures OpenTelemetry
├─────────────────────────────────────────┤
│         OpenTelemetry Android           │ ← Upstream instrumentation
│  ├── Activity Instrumentation          │
│  ├── Network Instrumentation           │
│  ├── Crash Instrumentation             │
│  └── ANR Instrumentation               │
├─────────────────────────────────────────┤
│            OTLP Exporters               │ ← Export to AWS
│  ├── Span Exporter → RUM Spans         │
│  └── Log Exporter → RUM Logs           │
└─────────────────────────────────────────┘
```

## Advantages over Manual Setup

- **Zero Code**: No need to modify your Application class
- **Automatic Configuration**: Reads settings from JSON file
- **Error Handling**: Built-in validation and error handling
- **AWS Optimized**: Pre-configured for AWS RUM endpoints
- **Easy Updates**: Configuration changes don't require code changes

## When to Use Manual Setup Instead

Consider using the [core module](../core/) for manual setup if you need:

- Dynamic configuration at runtime
- Custom authentication mechanisms
- Advanced OpenTelemetry customization
- Integration with existing telemetry systems
- Conditional instrumentation based on app state

## Troubleshooting

### No Telemetry Generated

1. Verify `aws_config.json` exists in `res/raw/`
2. Check that required fields (`region`, `appMonitorId`) are present
3. Ensure your RUM App Monitor exists and is active
4. Check device network connectivity

### Configuration Errors

The agent logs configuration errors to Android's system log:

```bash
adb logcat | grep "AWS Otel Android"
```

Common errors:
- Missing required fields
- Invalid JSON syntax
- Invalid App Monitor ID format

### Performance Impact

The agent is designed for minimal impact:
- Initializes asynchronously
- Uses efficient instrumentation
- Batches telemetry exports
- Respects Android's background limitations