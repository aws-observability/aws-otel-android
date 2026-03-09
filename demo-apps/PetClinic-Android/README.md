# Pet Clinic Android App

A simple Android frontend for the [application-signals-demo](https://github.com/aws-observability/application-signals-demo) PetClinic application.

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- Android SDK 34
- Minimum SDK 24 (Android 7.0)

## Building

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Run the app on an emulator or physical device

## Instrumentation with AWS OTEL Android

### 1. Add Dependencies

Add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:agent:1.0.0")
    
    // For HTTP instrumentation
    byteBuddy("io.opentelemetry.android.instrumentation:okhttp3-agent:0.15.0-alpha")
}
```

### 2. Create Configuration File

Create `app/src/main/res/raw/aws_config.json`:

```json
{
  "aws": {
    "region": "us-east-1",
    "rumAppMonitorId": "<your-app-monitor-id>"
  },
  "otelResourceAttributes": {
    "service.name": "PetClinic-Android",
    "service.version": "1.0.0",
    "deployment.environment": "production"
  }
}
```

### 3. That's it!

The agent automatically initializes and collects telemetry including:
- Activity lifecycle events
- Network requests
- Crashes and ANRs
- UI performance metrics
