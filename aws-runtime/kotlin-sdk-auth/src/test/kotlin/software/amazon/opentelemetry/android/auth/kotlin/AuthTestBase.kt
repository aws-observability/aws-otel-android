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
package software.amazon.opentelemetry.android.auth.kotlin

import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes

open class AuthTestBase {
    val testCredentials =
        Credentials.invoke(
            accessKeyId = "AKIDEXAMPLE",
            secretAccessKey = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY",
        )

    val credentialsProvider =
        object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials = testCredentials
        }

    val failingCredentialsProvider =
        object : CredentialsProvider {
            override suspend fun resolve(attributes: Attributes): Credentials = throw IllegalStateException("Failed to get credentials")
        }
}
