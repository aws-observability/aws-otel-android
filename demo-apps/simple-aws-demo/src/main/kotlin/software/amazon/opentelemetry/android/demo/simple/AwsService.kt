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
