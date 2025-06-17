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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.amazon.opentelemetry.android.demo.agent.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

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
        addInstrumentationTestFragment()
    }

    private fun setupUI() {
        binding.buttonPerformAction.setOnClickListener {
            navigateToSecondActivity()
        }
    }

    private fun addInstrumentationTestFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        
        val fragment = InstrumentationTestFragment.newInstance()
        fragmentTransaction.add(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
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
