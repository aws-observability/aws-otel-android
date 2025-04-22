# AWS OpenTelemetry Android Simple AWS Demo App Infrastructure

This CDK project creates the necessary AWS infrastructure for the AWS OpenTelemetry Android Simple AWS Demo App.

## Infrastructure Components

- **Cognito Identity Pool**: Provides authentication and authorization for the Android app
- **IAM Roles**: Defines permissions for authenticated and unauthenticated users with access to:
  - S3 operations
  - CloudWatch RUM PutRumEvents
  - CloudWatchAgentServerPolicy (for OTLP endpoints, X-Ray, and CloudWatch)
- **S3 Bucket**: A demo bucket that the app can access
- **CloudWatch RUM AppMonitor**: For OpenTelemetry instrumentation

## Deployment Instructions

### Prerequisites

- AWS CLI configured with appropriate credentials
- Node.js and npm installed
- AWS CDK installed (`npm install -g aws-cdk`)

### Steps to Deploy

1. Install requirements:
   - Node + NPM 
   - AWS CDK (https://docs.aws.amazon.com/cdk/v2/guide/getting-started.html)
2. Run `./demo-cdk.sh synth` then `./demo-cdk.sh deploy` with valid environment credentials.
3. Note the outputs from the deployment:
   - IdentityPoolId: Use this in your Android app's MainActivity.kt
   - AppMonitorId: Use this in your Android app's SimpleAwsDemoApplication.kt
   - Region: Use this in both files
   - DemoBucketName: Optional, for additional S3 operations

### Update Your Android App

After deploying the infrastructure, update the following files in your Android app:

1. `SimpleAwsDemoApplication.kt`:
   ```kotlin
   val appMonitorConfig = AwsRumAppMonitorConfig(
       region = "YOUR_REGION_FROM_OUTPUT",
       appMonitorId = "YOUR_APP_MONITOR_ID_FROM_OUTPUT"
   )
   ```

2. `MainActivity.kt`:
   ```kotlin
   private val cognitoPoolId = "YOUR_IDENTITY_POOL_ID_FROM_OUTPUT"
   private val awsRegion = Regions.YOUR_REGION_FROM_OUTPUT
   ```

## Cleanup

To remove all resources created by this stack:

```
cdk destroy
```

Note: This will delete all resources including the S3 bucket and its contents.
