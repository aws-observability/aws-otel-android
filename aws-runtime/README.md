# ADOT Android AWS Runtime

The AWS Runtime modules provide authentication and AWS-specific exporters for ADOT Android, enabling secure telemetry export to AWS services with proper authentication mechanisms.

## Modules

- **[kotlin-sdk-auth](kotlin-sdk-auth/)** - SigV4 signing with AWS SDK for Kotlin

## What it does

- **AWS Authentication**: AWS SDK credentials
- **SigV4 Signing**: Automatic request signing for AWS services
- **Secure Export**: OTLP exporters with AWS authentication

## Quick Start

### AWS SDK Authentication

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:kotlin-sdk-auth:1.0.0-alpha")
}
```

```kotlin
// Use any AWS SDK for Kotlin CredentialsProvider
val credentialsProvider = StaticCredentialsProvider {
    Credentials.invoke(
        accessKeyId = "your-access-key",
        secretAccessKey = "your-secret-key"
    )
}

OpenTelemetryAgent.Builder(this)
    .setAppMonitorConfig(config)
    .addSpanExporterCustomizer { _ ->
        AwsSigV4SpanExporter.builder()
            .setEndpoint("https://dataplane.rum.us-east-1.amazonaws.com/v1/traces")
            .setRegion("us-east-1")
            .setServiceName("rum")
            .setCredentialsProvider(credentialsProvider)
            .build()
    }
    .build()
```