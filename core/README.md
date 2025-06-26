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

## Advanced Configuration

### Custom Sampling

```kotlin
OpenTelemetryAgent.Builder(this)
    .setAppMonitorConfig(config)
    .setTracerSampler(Sampler.create(0.1)) // Sample 10% of traces
    .build()
```

### Disk Buffering

```kotlin
val diskBufferingConfig = DiskBufferingConfig(
    enabled = true,
    maxCacheSize = 10_000_000 // 10MB
)

OpenTelemetryAgent.Builder(this)
    .setAppMonitorConfig(config)
    .setDiskBufferingConfig(diskBufferingConfig)
    .build()
```

### Custom Exporters

```kotlin
OpenTelemetryAgent.Builder(this)
    .setAppMonitorConfig(config)
    .addSpanExporterCustomizer { defaultExporter ->
        // Replace or wrap the default span exporter
        OtlpHttpSpanExporter.builder()
            .setEndpoint("https://custom-endpoint.com/v1/traces")
            .setHeaders(mapOf("Authorization" to "Bearer token"))
            .build()
    }
    .addLogRecordExporterCustomizer { defaultExporter ->
        // Replace or wrap the default log exporter
        OtlpHttpLogRecordExporter.builder()
            .setEndpoint("https://custom-endpoint.com/v1/logs")
            .build()
    }
    .build()
```
