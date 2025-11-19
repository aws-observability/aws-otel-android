# AWS Distro for OpenTelemetry - Android

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview

This repo contains the AWS Distro for OpenTelemetry (ADOT) on Android. It is a bundle that ships a few things out of the box:
1. OpenTelemetry Java SDK (https://github.com/open-telemetry/opentelemetry-java)
2. OpenTelemetry Android automated instrumentation (https://github.com/open-telemetry/opentelemetry-android)
   - This includes instrumentation for telemetry like ANRs, Crashes, Activity lifecycle, etc
4. Additional automated instrumentation not currently part of upstream OpenTelemetry Android, like the "Time to first draw" telemetry
5. AWS-specific exporter and auth configuration that works out of the box when exporting telemetry to AWS CloudWatch Real User Monitoring (RUM)

The distro is preconfigured for seamless integration with [AWS CloudWatch RUM](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-RUM.html).

**Key Benefits:**
- **Zero-code instrumentation** for most common Android telemetry
- **AWS-native integration** with CloudWatch RUM and Application Signals
- **Production-ready** with minimal performance overhead
- **Comprehensive monitoring** including crashes, ANRs, UI performance, and network requests
- **Flexible configuration** supporting both programmatic and configuration file setup

## Quick Start

### 1. Add Dependencies

Add to your app's `build.gradle`. For a Kotlin DSL example:

```kotlin
dependencies {
    // For automatic instrumentation (recommended; see below for programmatic configuration)
    implementation("software.amazon.opentelemetry.android:agent:0.0.0")

    // For HTTP instrumentation with ByteBuddy
    byteBuddy("io.opentelemetry.android.instrumentation:okhttp3-agent:0.12.0-alpha")           // if you are using OkHttp-3.0
    byteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:0.12.0-alpha") // if you are using URLConnection / HttpURLConnection / HttpsURLConnection
}
```

### 2. Configuration

#### Option A: Zero-Code Configuration (Agent)

Create `res/raw/aws_config.json`:

```jsonc
{
  "aws": {
    "region": "<your region>",
    "rumAppMonitorId": "<your app monitor id>",

    // optional, if you have a resource-based policy with an alias
    "rumAlias": "<your rum alias"
  },

  // optional resource attributes, but recommended
  "otelResourceAttributes": {
    "service.name": "MyApplication",
    "service.namespace": "MyTeam",
    "service.version": "1.0.0",
    "deployment.environment": "production"
    // ... plus any additional custom resource attributes you want to define
  }
}
```

That's it! That's the minimum you need to automatically initialize the Android agent and start collecting telemetry into your RUM app monitor.

#### Option B: Programmatic Configuration

If you want to configure the client programmatically, you can depend on the lightweight **core** module instead of the **agent**:

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:core:LATEST_VERSION")
}
```

```kotlin
import io.opentelemetry.sdk.resources.Resource
import software.amazon.opentelemetry.android.OpenTelemetryRumClient

class MyApplication : Application() {
   override fun onCreate() {
      super.onCreate()

      OpenTelemetryRumClient {
         androidApplication = this@MyApplication
         awsRum {
            region = "us-east-1"
            appMonitorId = "<your-app-monitor-id>"
         }
         otelResource = Resource.builder()
            .put("service.name", "testAppName")
            .put("service.version", "1.0")
            .build()
      }
   }
}
```

- [Read more about custom instrumentation you can manually enable](docs/custom_instrumentation.md)
- [Read more about configuring the SDK with authentication](docs/auth.md)

## Feature modules

The library consists of several modules designed for flexibility and modularity:

| Module                                   | Description                                 | Use Case                           |
|------------------------------------------|---------------------------------------------|------------------------------------|
| **[agent](agent/README.md)**             | Zero-code auto-instrumentation              | Easiest setup, automatic telemetry |
| **[core](core/README.md)**               | Main OpenTelemetry client and configuration | Manual setup, full control         |
| **[api](api/README.md)**                 | Public API for custom instrumentation       | Adding custom spans and metrics    |
| **[ui-loading](ui-loading/README.md)**   | UI performance monitoring                   | Time-to-first-draw tracking        |
| **[aws-runtime](aws-runtime/README.md)** | AWS authentication and exporters            | SigV4 signing                      |

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
| **Sessions**           | Reports when user sessions start or end      | Automatic                                                  |

## Requirements

- **Android API Level**: 26 (Android 8.0) or higher
- **Kotlin**: 1.8.0 or higher
- **Java**: 8 or higher
- **Gradle**: 7.0 or higher

## Examples

Explore our comprehensive [demo applications](demo-apps/):

- **[Agent Demo](demo-apps/agent-demo/)** - Zero-configuration auto-instrumentation
- **[Crash Demo](demo-apps/crash-demo/)** - Crash reporting demonstration
- **[ANR Demo](demo-apps/anr-demo/)** - ANR detection example

## Version Management

### Version Bumping

The repository includes a script to manage version bumping across relevant files. The script updates versions in:
- `gradle.properties` (project version)
- `README.md` (documentation references, if any)

#### Manual Version Bumping

Use the `scripts/bump-version.sh` script for version control:

**Patch Version** (x.y.z → x.y.z+1):
```bash
./scripts/bump-version.sh patch
```

**Minor Version** (x.y.z → x.y+1.0):
```bash
./scripts/bump-version.sh minor
```

**Major Version** (x.y.z → x+1.0.0):
```bash
./scripts/bump-version.sh major
```

**Specific Version**:
```bash
./scripts/bump-version.sh 2.1.3
```

**With Automatic Commit and Tag**:
```bash
./scripts/bump-version.sh patch --commit-tag
```

The script will prompt for confirmation before making changes and creates backups of modified files.

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

## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public github issue.

## Related Projects

- **[OpenTelemetry Android](https://github.com/open-telemetry/opentelemetry-android)** - Upstream OpenTelemetry Android SDK
- **[AWS SDK for Android](https://github.com/aws-amplify/aws-sdk-android)** - AWS SDK for Android applications
- **[AWS CloudWatch RUM](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-RUM.html)** - Real User Monitoring service
- **[AWS Application Signals](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-Application-Signals.html)** - Application performance monitoring
