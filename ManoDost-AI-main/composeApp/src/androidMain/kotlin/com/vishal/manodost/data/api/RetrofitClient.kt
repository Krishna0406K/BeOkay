package com.vishal.manodost.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client Singleton for API Communication
 */
object RetrofitClient {
    
    // IMPORTANT: Using localhost with ADB reverse for physical device
    // ADB reverse command: adb reverse tcp:8000 tcp:8000
    // This forwards device's localhost:8000 to computer's localhost:8000
    private const val BASE_URL = "http://localhost:8000/"
    
    // Voice API runs on port 8001
    private const val VOICE_BASE_URL = "http://localhost:8001/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val voiceRetrofit = Retrofit.Builder()
        .baseUrl(VOICE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val apiServiceSimple: ApiServiceSimple = retrofit.create(ApiServiceSimple::class.java)
    val voiceApiService: VoiceApiService = voiceRetrofit.create(VoiceApiService::class.java)
}
