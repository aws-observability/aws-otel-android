package com.example.petclinic.data.network

/**
 * Configuration for Pet Clinic API
 */
object ApiConfig {
    
    // Default to Android emulator localhost
    // Change this to your actual API gateway URL when deploying
    const val DEFAULT_BASE_URL = "http://a61dafdaceaa340d5b3fc9d3a00f71c6-746455772.us-east-1.elb.amazonaws.com"
    
    // Alternative URLs for different environments
    const val LOCALHOST_BASE_URL = "http://localhost:8080"
    const val PRODUCTION_BASE_URL = "https://your-api-gateway-url.com"
    
    /**
     * Get the appropriate base URL based on the environment
     */
    fun getBaseUrl(environment: Environment = Environment.DEVELOPMENT): String {
        return when (environment) {
            Environment.DEVELOPMENT -> DEFAULT_BASE_URL
            Environment.LOCALHOST -> LOCALHOST_BASE_URL
            Environment.PRODUCTION -> PRODUCTION_BASE_URL
        }
    }
    
    enum class Environment {
        DEVELOPMENT,
        LOCALHOST,
        PRODUCTION
    }
}
