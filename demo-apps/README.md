## Demo Applications

This directory contains the set of demo app submodules for AWS OpenTelemetry Android.

### Requirements
- AWS CDK v2 installed
- An AWS account with appropriate permissions to deploy CDK (CloudFormation) stacks
- Docker and Docker Compose (for local otel-collector setup)
- Android Studio (latest version recommended)

## Running a demo app with Android Studio

### Setting Up an Android Emulator

1. **Open AVD Manager**
    - Click on "Tools" > "Device Manager" in the top menu
    - Click on "Create Virtual Device" button

2. **Select a Device Definition**
    - Choose a phone device (e.g., Pixel 6)
    - Click "Next"

3. **Select a System Image**
    - Choose a system image with API level 21 or higher (e.g., API 33 - Android 13)
    - If the system image is not installed, click the "Download" link next to it
    - Click "Next" after selecting the system image

4. **Configure the Virtual Device**
    - Give your emulator a name
    - Review other settings (default settings are usually fine)
    - Click "Finish"

For more detailed instructions on setting up an emulator, refer to the [Android Developer documentation](https://developer.android.com/studio/run/managing-avds).

### Running the App

1. **Select the Demo App Module**
    - In the Project panel, expand "demo-apps"
    - Right-click on any demo app (ie "simple-aws-demo") and select "Run 'simple-aws-demo'"
    - Alternatively, select the demo app (ie "simple-aws-demo") from the configuration dropdown in the toolbar and click the Run button

2. **Select Deployment Target**
    - Choose the emulator you created earlier
    - Click "OK"

3. **Wait for Deployment**
    - Android Studio will build the app and deploy it to the emulator
    - The emulator will launch and the app will start automatically

### Troubleshooting Common Issues

- **Gradle Build Failures**
    - Make sure you have the latest Android Studio version
    - Try "File" > "Sync Project with Gradle Files"
    - Check that your `local.properties` file has the correct SDK path

- **Emulator Performance Issues**
    - Enable hardware acceleration in the AVD settings
    - Allocate more RAM to the emulator
    - Consider using a physical device for testing

- **App Crashes on Launch**
    - Check that you've properly configured the AWS credentials as described in the "App Setup" section
    - Look at the Logcat output in Android Studio for error messages

- **Network Connection Issues**
    - Ensure the emulator has internet access
    - If using a local collector, make sure to use the special IP `10.0.2.2` to access your host machine from the emulator

For more help with Android Studio and emulator issues, see the [Android Studio troubleshooting guide](https://developer.android.com/studio/troubleshoot).