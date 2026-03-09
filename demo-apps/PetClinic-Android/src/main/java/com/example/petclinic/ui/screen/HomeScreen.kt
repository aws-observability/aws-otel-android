package com.example.petclinic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petclinic.data.network.ApiConfig
import kotlinx.coroutines.delay

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    var isJanking by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to Pet Clinic",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Application Signals Demo App",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${ApiConfig.getBaseUrl()}/images/pets.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Pet Clinic Pets",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "About This App",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "This Pet Clinic mobile app demonstrates AWS Application Signals capabilities. " +
                            "You can manage pet owners, view veterinarians, and track pet visits - all while " +
                            "Application Signals monitors the performance and health of the underlying microservices.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Features:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val features = listOf(
                    "• Browse and search pet owners",
                    "• View owner details and pet information",
                    "• Add new owners and pets",
                    "• View veterinarian information",
                    "• Track pet visits and medical history"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Testing Buttons Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ Testing & Monitoring",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "These buttons demonstrate different types of issues for Application Signals monitoring:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Crash Button
                Button(
                    onClick = {
                        throw RuntimeException("Intentional crash for Application Signals testing")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💥 Trigger App Crash")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // ANR Button
                Button(
                    onClick = {
                        // Block the main thread to cause ANR
                        Thread.sleep(10000) // 10 seconds - will cause ANR
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B00), // Orange
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⏰ Trigger ANR (10s block)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // UI Jank Button
                Button(
                    onClick = {
                        isJanking = !isJanking
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJanking) Color(0xFF4CAF50) else Color(0xFFFF9800), // Green when active, Orange when inactive
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isJanking) "✅ Stop UI Jank" else "🐌 Start UI Jank")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // HTTP 500 Error Button
                Button(
                    onClick = {
                        Thread {
                            try {
                                val client = okhttp3.OkHttpClient()
                                val request = okhttp3.Request.Builder()
                                    .url("https://httpbin.org/status/500")
                                    .build()
                                client.newCall(request).execute()
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }.start()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0), // Purple
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🌐 HTTP 500 Error")
                }
                
                if (isJanking) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "UI Jank is active - causing slow rendering...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Jank-inducing component when active
        if (isJanking) {
            JankInducingComponent()
        }
    }
}

@Composable
fun JankInducingComponent() {
    // This component will cause UI jank by doing expensive operations on the main thread
    LaunchedEffect(Unit) {
        while (true) {
            // Simulate expensive computation on main thread
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 100) {
                // Busy wait for 100ms - this will cause frame drops
                Math.sqrt(Math.random() * 1000000)
            }
            delay(50) // Small delay before next jank
        }
    }
    
    // Visual indicator that jank is happening
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Yellow.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.Red,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Causing UI Jank...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}
