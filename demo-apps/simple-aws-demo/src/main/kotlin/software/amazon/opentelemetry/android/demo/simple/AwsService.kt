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
import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.Bucket
import aws.sdk.kotlin.services.s3.model.ListBucketsRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import software.amazon.opentelemetry.android.auth.CognitoCachedCredentialsProvider

/**
 * Service class to handle AWS API calls
 */
class AwsService(
    private val cognitoPoolId: String,
    private val awsRegion: String
) {

    private val cognitoClient = CognitoIdentityClient {
        this.region = awsRegion
    }

    val cognitoCredentialsProvider = CognitoCachedCredentialsProvider(
        cognitoPoolId = cognitoPoolId,
        cognitoClient = cognitoClient,
    )

    /**
     * List S3 buckets
     */
    suspend fun listS3Buckets(): List<Bucket> {
        // Create S3 client
        val s3Client = S3Client {
            region = awsRegion
            credentialsProvider = cognitoCredentialsProvider
        }
        
        // List buckets
        val response = s3Client.listBuckets(ListBucketsRequest {})
        
        // Return the buckets
        return response.buckets ?: emptyList()
    }

    suspend fun getCognitoIdentityId(): String {
        return cognitoClient.getId(GetIdRequest {
            this.identityPoolId = cognitoPoolId
        }).identityId!!
    }

}
