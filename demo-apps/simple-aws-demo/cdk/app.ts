#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AwsOtelAndroidSimpleAwsDemoAppStack } from './lib/aws-otel-android-simple-aws-demo-app-stack';

const app = new cdk.App();
new AwsOtelAndroidSimpleAwsDemoAppStack(app, 'AwsOtelAndroidSimpleAwsDemoAppStack');
