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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import software.amazon.opentelemetry.android.demo.agent.SecondActivity.Companion.HTTP_200_URL
import software.amazon.opentelemetry.android.demo.agent.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private lateinit var fakeTtfdFragmentSpan: Span

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        setupUI()
    }

    private fun setupUI() {
        binding.buttonPerformAction.setOnClickListener {
            navigateToSecondActivity()
        }
        binding.simpleHttpCall.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val url = URL("https://www.android.com/")
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
            }
        }
        binding.okHttpCall.setOnClickListener {
            lifecycleScope.launch {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://www.android.com/")
                    .build()

                val result = withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        response.body?.string() ?: ""
                    }
                }
            }
        }
    }

    private fun navigateToSecondActivity() {
        Log.d(TAG, "Navigating to SecondActivity")
        
        lifecycleScope.launch {
            // Show loading state
            binding.textViewResult.text = "Preparing to navigate..."
            
            // Simulate some preparation work
            withContext(Dispatchers.IO) {
                delay(500) // Short delay to simulate work
            }
            
            // Create intent and add data
            val intent = Intent(this@MainActivity, SecondActivity::class.java).apply {
                putExtra("MESSAGE", "Hello from MainActivity! Timestamp: ${System.currentTimeMillis()}")
            }
            
            // Start the second activity
            startActivity(intent)
        }
    }
}
