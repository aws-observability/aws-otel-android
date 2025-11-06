# ADOT Android Core

The ADOT Android Core module provides the foundational OpenTelemetry implementation with AWS-specific configurations. It offers programmatic control over instrumentation setup, allowing for advanced customization and integration scenarios that require more flexibility than the zero-code agent approach.

## Quick Start

### 1. Add Dependencies

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:core:LATEST_VERSION")
}
```

### 2. Initialize in Application Class

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OpenTelemetryRumClient {
            androidApplication = this@MyApplication
            awsRum {
                region = "us-east-1"
                appMonitorId = "<your-app-monitor-id>"
                alias = "<your-resource-based-policy-alias>"
            }
            sessionInactivityTimeout = Duration.ofMinutes(1)
            applicationAttributes = mapOf("app.test" to "123")
            serviceVersion = "1.0"
        }
    }
}
```

## Advanced Configuration

### Custom Sampling

```kotlin
OpenTelemetryRumClient {
    // ... <all prior configuration>

    tracerSampler = Sampler.create(0.1) // sample 10% of traces
}
```

### Disk Buffering

```kotlin
OpenTelemetryRumClient {
    // ... <all prior configuration>

    diskBuffering {
        enabled = true
        maxCacheSize = 10_000_000
    }
}
```

### Custom Exporters

```kotlin

OpenTelemetryRumClient {
    // ... <all prior configuration>

    spanExporter = OtlpHttpSpanExporter.builder()
        .setEndpoint("https://custom-endpoint.com/v1/traces")
        .setHeaders(mapOf("Authorization" to "Bearer token"))
        .build()

    logRecordExporter = OtlpHttpLogRecordExporter.builder()
        .setEndpoint("https://custom-endpoint.com/v1/logs")
        .build()
}
```
