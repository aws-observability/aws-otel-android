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
import software.amazon.opentelemetry.android.demo.agent.databinding.ActivitySecondBinding

class CustomException(message: String) : Exception(message)

class SecondActivity : AppCompatActivity() {

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

        Log.d(TAG, "Second Activity created")

        // Get any data passed from the first activity
        val message = intent.getStringExtra("MESSAGE") ?: "No message received"
        binding.textViewMessage.text = message

        awsService = AwsService(cognitoPoolId, awsRegion)

        setupButtons()
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
                            val url = URL("https://www.android.com")
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
}
