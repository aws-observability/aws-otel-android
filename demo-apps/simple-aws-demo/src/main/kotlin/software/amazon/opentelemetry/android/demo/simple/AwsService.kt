/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.opentelemetry.android.demo.simple

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.Bucket
import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider

/**
 * Service class to handle AWS API calls
 */
class AwsService(
    private val awsRegion: String
) {

    /**
     * List S3 buckets
     * Note: This requires proper AWS credentials to be configured
     */
    suspend fun listS3Buckets(): List<Bucket> {
        // Create S3 client with default credentials provider
        val s3Client = S3Client {
            region = awsRegion
            // Note: You'll need to configure proper credentials
            // This could be through cognito or some other custom credentials provider
        }
        
        // List buckets
        val response = s3Client.listBuckets(ListBucketsRequest {})
        
        // Return the buckets
        return response.buckets ?: emptyList()
    }
}
