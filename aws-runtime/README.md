# ADOT Android AWS Runtime

The AWS Runtime modules provide authentication and AWS-specific exporters for ADOT Android, enabling secure telemetry export to AWS services with proper authentication mechanisms.

## Modules

- **[kotlin-sdk-auth](kotlin-sdk-auth/)** - SigV4 signing with AWS SDK for Kotlin
- **[cognito-auth](cognito-auth/)** - Cognito Identity Pool authentication

## What it does

- **AWS Authentication**: Cognito Identity Pools and AWS SDK credentials
- **SigV4 Signing**: Automatic request signing for AWS services
- **Secure Export**: OTLP exporters with AWS authentication
- **Credential Caching**: Efficient credential management and refresh when using Cognito Identity Pools

## Quick Start

### Option 1: Authentication with Cognito

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:cognito-auth:LATEST_VERSION")
}
```

```kotlin
val credentialsProvider = CognitoCachedCredentialsProvider(
    cognitoPoolId = "us-east-1:your-identity-pool-id",
    cognitoClient = cognitoIdentityClient
)

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

### Option 2: Generic AWS SDK Authentication

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

// Same exporter setup as above
```