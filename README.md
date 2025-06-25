# AWS Distro for OpenTelemetry - Android

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview

This repo contains the AWS Distro for OpenTelemetry (ADOT) on Android. It is a bundle that ships a few things out of the box:
1. OpenTelemetry Java SDK (https://github.com/open-telemetry/opentelemetry-java)
2. OpenTelemetry Android automated instrumentation (https://github.com/open-telemetry/opentelemetry-android)
   3. This includes instrumentation for telemetry like ANRs, Crashes, Activity lifecycle, etc
4. UI component loading instrumentation (for "Time to first draw" telemetry)
5. AWS-specific exporter and auth configuration that works out of the box when exporting telemetry to AWS CloudWatch Real User Monitoring

The distro is preconfigured for seamless integration with AWS RUM.

**Key Benefits:**
- **Zero-code instrumentation** for most common Android telemetry
- **AWS-native integration** with CloudWatch RUM and Application Signals
- **Production-ready** with minimal performance overhead
- **Comprehensive monitoring** including crashes, ANRs, UI performance, and network requests
- **Flexible configuration** supporting both programmatic and configuration file setup

## Quick Start

### 1. Add Dependencies

Add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // For automatic instrumentation (recommended; see below for programmatic configuration)
    implementation("software.amazon.opentelemetry.android:agent:LATEST_VERSION")
    
    // For ByteBuddy instrumentation (optional)
    byteBuddy("io.opentelemetry.android:instrumentation-okhttp-3.0-agent:LATEST_VERSION")           // if you are using OkHttp-3.0
    byteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:LATEST_VERSION")    // if you are using URLConnection / HttpURLConnection / HttpsURLConnection
}
```

### 2. Configuration

#### Option A: Zero-Code Configuration (Agent)

Create `res/raw/aws_config.json`:

```json
{
  "rum": {
    "region": "us-east-1", // or whatever region you care to monitor in
    "appMonitorId": "<your-app-monitor-id>",
    "alias": "<your-app-alias>",
  },
  "application": {
    "applicationVersion": "1.0.0"
  }
}
```

That's it! The agent will automatically initialize and start collecting telemetry.

#### Option B: Programmatic Configuration

If you want to configure the client programmatically, you can depend on the lightweight **core** module instead of the **agent**:

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:core:LATEST_VERSION")
}
```

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = AwsRumAppMonitorConfig(
            region = "us-east-1",
            appMonitorId = "your-app-monitor-id",
            alias = "your-app-alias"
        )
        
        OpenTelemetryAgent.Builder(this)
            .setAppMonitorConfig(config)
            .setApplicationVersion("1.0.0")
            .build()
    }
}
```

### 3. Custom Instrumentation (Optional)

```kotlin
import software.amazon.opentelemetry.android.api.AwsRum

// Create custom spans
val span = AwsRum.startSpan(
    name = "user_action",
    screenName = "MainActivity",
    attributes = mapOf("action" to "button_click")
)
// ... perform work
span.end()

// Or use executeSpan for automatic lifecycle management
AwsRum.executeSpan("database_query") { span ->
    // Your code here - span is automatically ended
    performDatabaseQuery()
}
```

## Architecture

The library consists of several modules designed for flexibility and modularity:

| Module                                   | Description                                 | Use Case |
|------------------------------------------|---------------------------------------------|----------|
| **[agent](agent/README.md)**             | Zero-code auto-instrumentation              | Easiest setup, automatic telemetry |
| **[core](core/README.md)**               | Main OpenTelemetry client and configuration | Manual setup, full control |
| **[api](api/README.md)**                 | Public API for custom instrumentation       | Adding custom spans and metrics |
| **[ui-loading](ui-loading/README.md)**   | UI performance monitoring                   | Time-to-first-draw tracking |
| **[aws-runtime](aws-runtime/README.md)** | AWS authentication and exporters            | Cognito auth, SigV4 signing |

## Supported Instrumentation

| Instrumentation        | Telemetry generated                          | Automatic or Manual                                        |
|------------------------|----------------------------------------------|------------------------------------------------------------|
| **Activity Lifecycle** | Activity creation, start, resume tracking    | Automatic                                                  |
| **Fragment Lifecycle** | Fragment lifecycle and navigation            | Automatic                                                  |
| **Network Requests**   | HTTP/HTTPS via URLConnection and OkHttp      | Automatic (w/ ByteBuddy)                                   |
| **Crash Reporting**    | Unhandled exceptions and system crashes      | Automatic                                                  |
| **ANR Detection**      | Application Not Responding events            | Automatic                                                  |
| **UI Performance**     | Time-to-first-draw, slow rendering           | Automatic for Activities (manual Fragment instrumentation) |
| **Custom Spans**       | Application-specific instrumentation         | Manual                                                     |
| **Slow rendering**     | Reports when app interface is slow or frozen | Automatic                                                  |

## Requirements

- **Android API Level**: 26 (Android 8.0) or higher
- **Kotlin**: 1.8.0 or higher
- **Java**: 8 or higher
- **Gradle**: 7.0 or higher

## Examples

Explore our comprehensive [demo applications](demo-apps/):

- **[Simple AWS Demo](demo-apps/simple-aws-demo/)** - Basic CloudWatch RUM integration
- **[Agent Demo](demo-apps/agent-demo/)** - Zero-configuration auto-instrumentation
- **[Crash Demo](demo-apps/crash-demo/)** - Crash reporting demonstration
- **[ANR Demo](demo-apps/anr-demo/)** - ANR detection example

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Setting up the development environment
- Running tests and building the project
- Submitting pull requests
- Reporting issues

## Support

- **GitHub Issues**: For bug reports and feature requests
- **AWS Support**: Enterprise customers can create support tickets
- **Documentation**: Comprehensive guides in the [docs/](docs/) directory
- **Community**: Join discussions in GitHub Discussions

## License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

## Related Projects

- **[OpenTelemetry Android](https://github.com/open-telemetry/opentelemetry-android)** - Upstream OpenTelemetry Android SDK
- **[AWS SDK for Android](https://github.com/aws-amplify/aws-sdk-android)** - AWS SDK for Android applications
- **[AWS CloudWatch RUM](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-RUM.html)** - Real User Monitoring service
- **[AWS Application Signals](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-Application-Signals.html)** - Application performance monitoring
