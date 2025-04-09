# AWS Distro for OpenTelemetry - Instrumentation for Android

## Introduction

This project is a redistribution of the [OpenTelemetry Android SDK](https://github.com/open-telemetry/opentelemetry-android),
preconfigured for use with AWS services. Please check out that project too to get a better
understanding of the underlying internals. You won't see much code in this repository since we only
apply some small configuration changes, and our OpenTelemetry friends take care of the rest. The
exception to this is support for CloudWatch RUM and Application Signals.

We provide an Android library that can be consumed within any Native Android application using
supported Android API (level 21 and above). We build convenience functions to onboard your
application with OpenTelemetry and start ingesting telemetry into your CloudWatch RUM Application
Monitors.

## Getting Started

TBD

## Android API Compatibility

TBD

## Support

Please note that as per policy, we're providing support via GitHub on a best effort basis. However, 
if you have AWS Enterprise Support you can create a ticket and we will provide direct support within
the respective SLAs.

## Security issue notifications
If you discover a potential security issue in this project we ask that you notify AWS/Amazon 
Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). 
Please do **not** create a public github issue.

## License

This project is licensed under the Apache-2.0 License.