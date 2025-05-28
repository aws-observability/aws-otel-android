# Crash Demo App

This is a simple "Crash Demo" application for AWS OpenTelemetry Android. It demonstrates the basic structure of an Android application following the AWS OpenTelemetry Android conventions. It is used specifically to demonstrate the ability of AWS Opentelemetry Android to instrument Crash telemetry data 

## Features

- Displays a simple "Crash Demo" text on the screen with a counter. Application crashes after the countdown has reached 0
- Follows the same package structure and conventions as other AWS OpenTelemetry Android demo apps

## Running the App

1. Open the project in Android Studio
2. Select the `crash-demo` configuration from the run configurations dropdown
3. Click the Run button or press Shift+F10
4. Select an emulator or connected device to run the app on

## Project Structure

- `src/main/kotlin/software/amazon/opentelemetry/android/demo/crash/` - Contains the Kotlin source files
- `src/main/res/` - Contains the Android resources (layouts, strings, etc.)
- `src/main/AndroidManifest.xml` - The Android manifest file
