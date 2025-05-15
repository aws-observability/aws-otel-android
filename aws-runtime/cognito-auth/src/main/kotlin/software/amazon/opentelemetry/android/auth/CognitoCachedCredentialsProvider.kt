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
package software.amazon.opentelemetry.android.auth

import aws.sdk.kotlin.services.cognitoidentity.CognitoIdentityClient
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import aws.sdk.kotlin.services.cognitoidentity.model.Credentials as CognitoCredentials

/**
 * By default, if we're within this many seconds of the expiry for a credential set,
 * CognitoCachedCredentialsProvider will refresh credentials anyway. This can mitigate the risk of
 * failing / blocked AWS calls in the event of failure to fetch new credentials
 */
private const val DEFAULT_CREDENTIALS_REFRESH_BUFFER_SECONDS = 10

/**
 * A CredentialsProvider that pulls credentials from Cognito GetCredentialsForIdentity API and
 * caches them in memory with an expiration date that Cognito returns.
 *
 * This class handles refreshing credentials on-demand from the `resolve` function.
 */
class CognitoCachedCredentialsProvider(
    private val cognitoPoolId: String,
    private val cognitoClient: CognitoIdentityClient,
    private val loginsMap: Map<String, String>? = null,
    private val refreshBufferWindow: Duration = DEFAULT_CREDENTIALS_REFRESH_BUFFER_SECONDS.seconds,
    private val clock: Clock = Clock.System,
) : CredentialsProvider {
    private var cachedCredentials: CognitoCredentials? = null

    /**
     * Resolve credentials with Cognito enhanced authflow if either no credentials found or they
     * are expired. Otherwise, retrieve the existing cached credentials
     */
    override suspend fun resolve(attributes: Attributes): Credentials {
        if (shouldUpdateCredentials()) {
            val idResponse =
                cognitoClient.getId(
                    GetIdRequest {
                        this.identityPoolId = cognitoPoolId
                        this.logins = loginsMap
                    },
                )
            val identityId = idResponse.identityId

            val credentialsResponse =
                cognitoClient.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = identityId
                        this.logins = loginsMap
                    },
                )
            cachedCredentials = credentialsResponse.credentials
        }
        return Credentials.invoke(
            accessKeyId = cachedCredentials?.accessKeyId!!,
            secretAccessKey = cachedCredentials?.secretKey!!,
            sessionToken = cachedCredentials?.sessionToken,
            expiration = cachedCredentials?.expiration,
        )
    }

    private fun shouldUpdateCredentials(): Boolean {
        val checkCredentials = cachedCredentials
        if (checkCredentials?.expiration == null) {
            return true
        }
        if (clock.now().plus(refreshBufferWindow) >= checkCredentials.expiration!!) {
            return true
        }
        return false
    }
}
