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

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.ListBucketsRequest

/**
 * Service class to handle AWS API calls
 */
class AwsService(
    private val context: Context,
    private val cognitoPoolId: String,
    private val awsRegion: Regions
) {

    private val credentialsProvider = CognitoCachingCredentialsProvider(
        context,
        cognitoPoolId,
        awsRegion
    )

    /**
     * List S3 buckets
     */
    fun listS3Buckets(): List<Bucket> {
        val s3Client = AmazonS3Client(credentialsProvider, Region.getRegion(awsRegion))
        return s3Client.listBuckets(
            ListBucketsRequest().apply {

            }
        )
    }

    /**
     * Get Cognito identity
     */
    fun getCognitoIdentity(): String {
        return credentialsProvider.identityId
    }
}
