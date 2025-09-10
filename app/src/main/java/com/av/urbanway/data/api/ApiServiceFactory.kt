package com.av.urbanway.data.api

import com.google.gson.GsonBuilder
import com.av.urbanway.data.models.JourneyOption
import com.av.urbanway.data.models.JourneyOptionDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiServiceFactory {
    private const val BASE_URL = "https://av-gtfsfuncs.azurewebsites.net/"
    
    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(JourneyOption::class.java, JourneyOptionDeserializer())
            .setLenient()
            .create()
    }
    
    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    val urbanWayApiService: UrbanWayAPIService by lazy {
        retrofit.create(UrbanWayAPIService::class.java)
    }
}
