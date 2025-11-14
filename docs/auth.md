# Authentication Guide

This guide covers authentication options for AWS Distro for OpenTelemetry Android.

## Resource-Based Policy with Alias (Recommended)

The simplest approach uses a resource-based policy with an alias. This works with both **agent** and **core** modules and requires no credential management in your app.

### Setup

1. Create a resource-based policy in your RUM app monitor with an alias
2. Configure your app with the alias:

**Agent (zero-code)** - `res/raw/aws_config.json`:
```json
{
  "aws": {
    "region": "us-east-1",
    "rumAppMonitorId": "your-app-monitor-id",
    "rumAlias": "your-rum-alias"
  },
  "otelResourceAttributes": {
    "service.name": "MyApplication",
    "service.version": "1.0.0"
  }
}
```

**Core (programmatic)**:
```kotlin
import software.amazon.opentelemetry.android.OpenTelemetryRumClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OpenTelemetryRumClient {
            androidApplication = this@MyApplication
            awsRum {
                region = "us-east-1"
                appMonitorId = "your-app-monitor-id"
                alias = "your-rum-alias"
            }
            otelResource = Resource.builder()
                .put("service.name", "MyApplication")
                .put("service.version", "1.0.0")
                .build()
        }
    }
}
```

## SigV4 Authentication (Manual Configuration)

For advanced use cases requiring custom credential management, use SigV4-signed exporters with the **core** module.

### Dependencies

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:core:LATEST_VERSION")
    implementation("software.amazon.opentelemetry.android:aws-runtime:LATEST_VERSION")
}
```

### Basic Implementation

```kotlin
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.auth.AwsSigV4SpanExporter
import software.amazon.opentelemetry.android.auth.AwsSigV4LogRecordExporter

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val credentialsProvider = CredentialsProvider {
            Credentials(
                accessKeyId = "your-access-key-id",
                secretAccessKey = "your-secret-access-key",
                sessionToken = "your-session-token"
            )
        }

        OpenTelemetryRumClient {
            androidApplication = this@MyApplication
            
            spanExporter = AwsSigV4SpanExporter.builder()
                .setEndpoint("https://dataplane.rum.us-east-1.amazonaws.com/v1/rum")
                .setRegion("us-east-1")
                .setServiceName("rum")
                .setCredentialsProvider(credentialsProvider)
                .build()
                
            logRecordExporter = AwsSigV4LogRecordExporter.builder()
                .setEndpoint("https://dataplane.rum.us-east-1.amazonaws.com/v1/rum")
                .setRegion("us-east-1")
                .setServiceName("rum")
                .setCredentialsProvider(credentialsProvider)
                .build()
            
            otelResource = Resource.builder()
                .put("service.name", "MyApplication")
                .put("service.version", "1.0.0")
                .build()
        }
    }
}
```
