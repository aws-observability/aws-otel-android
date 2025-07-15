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
package software.amazon.opentelemetry.android.demo.agent

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import software.amazon.opentelemetry.android.api.AwsRum
import software.amazon.opentelemetry.android.demo.agent.databinding.ActivitySecondBinding
import java.io.IOException

class CustomException(message: String) : Exception(message)

class SecondActivity : AppCompatActivity() {

    companion object {
        const val HTTP_200_URL = "http://10.0.2.2:8181/200"
        const val HTTP_404_URL = "http://10.0.2.2:8181/404"
        const val HTTP_500_URL = "http://10.0.2.2:8181/500"
    }

    private lateinit var binding: ActivitySecondBinding
    private val TAG = "SecondActivity"

    // Replace these with your actual AWS credentials and configuration
    private val cognitoPoolId = "us-east-1:<ID>" // Replace with your Cognito Identity Pool ID
    private val awsRegion = "us-east-1" // Replace with your AWS region

    private lateinit var awsService: AwsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Ensure proper window insets handling for Android API level 35
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft,
                0, // We handle this with paddingTop in the layout
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )
            insets
        }

        Log.d(TAG, "Second Activity created")

        // Get any data passed from the first activity
        val message = intent.getStringExtra("MESSAGE") ?: "No message received"
        binding.textViewMessage.text = message

        AwsRum.executeSpan(
            name = "custom-parent-span",
            screenName = this.javaClass.simpleName
        ) {
            parent ->
                awsService = AwsService(cognitoPoolId, awsRegion)
                AwsRum.executeSpan(
                    name = "custom-child-span",
                    screenName = this.javaClass.simpleName,
                    parent
                ) {
                    setupButtons()
                }
        }
    }

    private fun setupButtons() {
        binding.listS3BucketsButton.setOnClickListener { listS3Buckets() }

        binding.getCognitoIdentityButton.setOnClickListener { getCognitoIdentity() }

        binding.buttonReturn.setOnClickListener {
            Log.d(TAG, "Returning to MainActivity")
            finish()
        }

        binding.crashButton.setOnClickListener {
            Log.d(TAG, "Crash test")
            crashApplicationTest()
        }

        binding.httpCallButton.setOnClickListener {
            Log.d(TAG, "Http Call button clicked")
            makeHttpCall()
        }
        
        binding.okhttp3.setOnClickListener {
            Log.d(TAG, "Ok Http3 call")
            makeOkHttp3Call()
        }
        
        binding.okhttp3Call400.setOnClickListener {
            Log.d(TAG, "OkHttp3 Call 400")
            makeOkHttp3Call400()
        }
        
        binding.okhttp3Call500.setOnClickListener {
            Log.d(TAG, "OkHttp3 Call 500")
            makeOkHttp3Call500()
        }
        
        binding.httpCall400Button.setOnClickListener {
            Log.d(TAG, "Http Call 400 button clicked")
            makeHttpCall400()
        }
        
        binding.httpCall500Button.setOnClickListener {
            Log.d(TAG, "Http Call 500 button clicked")
            makeHttpCall500()
        }
    }

    private fun crashApplicationTest() {
        throw CustomException("Testing Exception")
    }

    private fun listS3Buckets() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Loading S3 buckets..."
                val result =
                        withContext(Dispatchers.IO) {
                            val buckets = awsService.listS3Buckets()

                            // Build a string with bucket information
                            val sb = StringBuilder("S3 Buckets:\n\n")
                            buckets.forEach { bucket ->
                                sb.append("- ${bucket.name} (Created: ${bucket.creationDate})\n")
                            }
                            sb.toString()
                        }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.e(TAG, "Error listing S3 buckets", e)
                binding.resultTextView.text = "Error listing S3 buckets: ${e.message}"
                Toast.makeText(this@SecondActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun getCognitoIdentity() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Fetching Cognito identity..."
                val result =
                        withContext(Dispatchers.IO) {
                            val identityId = awsService.getCognitoIdentityId()
                            "Cognito Identity ID: $identityId"
                        }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Cognito identity", e)
                binding.resultTextView.text = "Error getting Cognito identity: ${e.message}"
                Toast.makeText(this@SecondActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun makeHttpCall() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP call..."

                val result =
                        withContext(Dispatchers.IO) {
                            val url = URL(HTTP_200_URL)
                            val urlConnection = url.openConnection() as HttpURLConnection
                            try {
                                val `in`: InputStream =
                                        BufferedInputStream(urlConnection.inputStream)
                                val response = `in`.bufferedReader().use { it.readText() }
                                response // Return the response
                            } finally {
                                urlConnection.disconnect() // Show the actual response
                            }
                        }

                binding.resultTextView.text = result // Show the actual response
            } catch (e: Exception) {
                Log.d(TAG, e.message!!)
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP call failed"
            }
        }
    }

    private fun makeOkHttp3Call() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP call..."

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(HTTP_200_URL)
                    .build()

                val result = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        response.body?.string() ?: ""
                    }
                }

                binding.resultTextView.text = result // Show the actual response
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Unknown error")
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP call failed"
            }
        }
    }
    
    private fun makeOkHttp3Call400() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP 400 call..."

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(HTTP_404_URL)
                    .build()

                val result = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        "Response code: ${response.code}, message: ${response.message}"
                    }
                }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Unknown error")
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP 400 call failed: ${e.message}"
            }
        }
    }

    private fun makeOkHttp3Call500() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP 400 call..."

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(HTTP_500_URL)
                    .build()

                val result = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        "Response code: ${response.code}, message: ${response.message}"
                    }
                }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Unknown error")
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP 400 call failed: ${e.message}"
            }
        }
    }

    private fun makeHttpCall400() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP 400 call using HttpURLConnection..."

                val result = withContext(Dispatchers.IO) {
                    val url = URL(HTTP_404_URL)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    try {
                        val responseCode = urlConnection.responseCode
                        val responseMessage = urlConnection.responseMessage
                        "Response code: $responseCode, message: $responseMessage"
                    } finally {
                        urlConnection.disconnect()
                    }
                }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Unknown error")
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP 400 call failed: ${e.message}"
            }
        }
    }

    private fun makeHttpCall500() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Making HTTP 500 call using HttpURLConnection..."

                val result = withContext(Dispatchers.IO) {
                    val url = URL(HTTP_500_URL)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    try {
                        val responseCode = urlConnection.responseCode
                        val responseMessage = urlConnection.responseMessage
                        "Response code: $responseCode, message: $responseMessage"
                    } finally {
                        urlConnection.disconnect()
                    }
                }

                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Unknown error")
                Log.e(TAG, "Http Error: ", e)
                binding.resultTextView.text = "HTTP 500 call failed: ${e.message}"
            }
        }
    }
}
