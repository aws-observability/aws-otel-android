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
import aws.sdk.kotlin.services.cognitoidentity.model.Credentials
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetCredentialsForIdentityResponse
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdRequest
import aws.sdk.kotlin.services.cognitoidentity.model.GetIdResponse
import aws.smithy.kotlin.runtime.time.Clock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import kotlin.time.Duration

@ExtendWith(MockKExtension::class)
class CognitoCachedCredentialsProviderTest {
    @MockK
    private lateinit var cognitoClient: CognitoIdentityClient

    @MockK
    private lateinit var mockClock: Clock

    private val currentTime = Instant.now()
    private val expirationTime = currentTime.plusSeconds(3600 * 3)

    private val testIdentityPoolId = "us-east-1:testidentitypoolid"
    private val testIdentityId = "us-east-1:testidentityid"
    private val testAccessKeyId = "AKIDEXAMPLE"
    private val testSecretKey = "secret"
    private val testSessionToken = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
    private val testLoginsMap = mapOf("provider" to "token")

    @BeforeEach
    fun setup() {
        every { mockClock.now() } returns
            aws.smithy.kotlin.runtime.time
                .Instant(currentTime)

        coEvery { cognitoClient.getId(any()) } returns
            GetIdResponse {
                this.identityId = testIdentityId
            }

        coEvery { cognitoClient.getCredentialsForIdentity(any()) } returns
            GetCredentialsForIdentityResponse {
                this.identityId = testIdentityId
                this.credentials =
                    Credentials {
                        this.accessKeyId = testAccessKeyId
                        this.secretKey = testSecretKey
                        this.sessionToken = testSessionToken
                        this.expiration =
                            aws.smithy.kotlin.runtime.time
                                .Instant(expirationTime)
                    }
            }
    }

    @Test
    fun `initial credentials resolution should pull new credentials`() =
        runTest {
            val provider =
                CognitoCachedCredentialsProvider(
                    cognitoPoolId = testIdentityPoolId,
                    cognitoClient = cognitoClient,
                    loginsMap = testLoginsMap,
                    clock = mockClock,
                )

            val credentials = provider.resolve()

            Assertions.assertEquals(testAccessKeyId, credentials.accessKeyId)
            Assertions.assertEquals(testSecretKey, credentials.secretAccessKey)
            Assertions.assertEquals(testSessionToken, credentials.sessionToken)
            Assertions.assertEquals(expirationTime.epochSecond, credentials.expiration!!.epochSeconds)

            coVerify(exactly = 1) {
                cognitoClient.getId(
                    GetIdRequest {
                        this.identityPoolId = testIdentityPoolId
                        this.logins = testLoginsMap
                    },
                )
                cognitoClient.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = testIdentityId
                        this.logins = testLoginsMap
                    },
                )
            }
        }

    @Test
    fun `initial credentials resolution followed by second credentials resolution should pull same credentials`() =
        runTest {
            val provider =
                CognitoCachedCredentialsProvider(
                    cognitoPoolId = testIdentityPoolId,
                    cognitoClient = cognitoClient,
                    loginsMap = testLoginsMap,
                    clock = mockClock,
                )

            val credentials1 = provider.resolve()
            val credentials2 = provider.resolve()

            coVerify(exactly = 1) {
                cognitoClient.getId(
                    GetIdRequest {
                        this.identityPoolId = testIdentityPoolId
                        this.logins = testLoginsMap
                    },
                )
                cognitoClient.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = testIdentityId
                        this.logins = testLoginsMap
                    },
                )
            }
        }

    @Test
    fun `credentials resolution after expiration time should pull new credentials`() =
        runTest {
            val provider =
                CognitoCachedCredentialsProvider(
                    cognitoPoolId = testIdentityPoolId,
                    cognitoClient = cognitoClient,
                    loginsMap = testLoginsMap,
                    clock = mockClock,
                )

            val credentials1 = provider.resolve()

            every { mockClock.now() } returns
                aws.smithy.kotlin.runtime.time
                    .Instant(expirationTime)

            val credentials2 = provider.resolve()

            coVerify(exactly = 2) {
                cognitoClient.getId(
                    GetIdRequest {
                        this.identityPoolId = testIdentityPoolId
                        this.logins = testLoginsMap
                    },
                )
                cognitoClient.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = testIdentityId
                        this.logins = testLoginsMap
                    },
                )
            }
        }

    @Test
    fun `credentials resolution within the refresh buffer window should pull new credentials`() =
        runTest {
            val provider =
                CognitoCachedCredentialsProvider(
                    cognitoPoolId = testIdentityPoolId,
                    cognitoClient = cognitoClient,
                    loginsMap = testLoginsMap,
                    refreshBufferWindow = Duration.parse("1h"),
                    clock = mockClock,
                )

            val credentials1 = provider.resolve()

            every { mockClock.now() } returns
                aws.smithy.kotlin.runtime.time
                    .Instant(expirationTime.minusSeconds(3600))

            val credentials2 = provider.resolve()

            coVerify(exactly = 2) {
                cognitoClient.getId(
                    GetIdRequest {
                        this.identityPoolId = testIdentityPoolId
                        this.logins = testLoginsMap
                    },
                )
                cognitoClient.getCredentialsForIdentity(
                    GetCredentialsForIdentityRequest {
                        this.identityId = testIdentityId
                        this.logins = testLoginsMap
                    },
                )
            }
        }
}
