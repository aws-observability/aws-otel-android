package com.example.petclinic.data.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP client configuration for Pet Clinic API
 */
object HttpClient {
    
    private const val TAG = "PetClinicHttpClient"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(createLoggingInterceptor())
            .addInterceptor(createErrorInterceptor())
            .build()
    }
    
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            // Use BASIC for better performance - logs URL, method, status code and time
            // BODY logs entire request/response which is VERY slow for large responses
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }
    
    private fun createErrorInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw ApiException.HttpException(
                    code = response.code,
                    message = "HTTP ${response.code}: $errorBody"
                )
            }
            
            response
        }
    }
}

/**
 * Extension functions for OkHttp Request building
 */
fun Request.Builder.jsonBody(json: String): Request.Builder {
    return this.post(json.toRequestBody("application/json".toMediaType()))
}

fun Request.Builder.putJsonBody(json: String): Request.Builder {
    return this.put(json.toRequestBody("application/json".toMediaType()))
}

/**
 * Extension function to safely get response body as string
 */
@Throws(IOException::class)
fun Response.getBodyString(): String {
    return body?.string() ?: throw IOException("Response body is null")
}
