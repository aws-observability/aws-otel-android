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

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.amazon.opentelemetry.android.demo.simple.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    private lateinit var awsService: AwsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = (applicationContext as SimpleAwsDemoApplication)
        awsService = app.awsService
        
        setupButtons()
    }

    private fun setupButtons() {
        binding.listS3BucketsButton.setOnClickListener {
            listS3Buckets()
        }

        binding.getCognitoIdentityButton.setOnClickListener {
            getCognitoIdentity()
        }
    }

    private fun listS3Buckets() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Loading S3 buckets..."
                val result = withContext(Dispatchers.IO) {
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
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCognitoIdentity() {
        lifecycleScope.launch {
            try {
                binding.resultTextView.text = "Fetching Cognito identity..."
                val result = withContext(Dispatchers.IO) {
                    val identityId = awsService.getCognitoIdentityId()
                    "Cognito Identity ID: $identityId"
                }
                
                binding.resultTextView.text = result
            } catch (e: Exception) {
                Log.e(TAG, "Error getting Cognito identity", e)
                binding.resultTextView.text = "Error getting Cognito identity: ${e.message}"
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
