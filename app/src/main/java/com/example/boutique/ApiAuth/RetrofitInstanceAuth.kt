package com.example.boutique.ApiAuth

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceAuth {
    private const val BASE_URL = "https://649d9368-3ac7-44de-bc74-c3e0315c25b8-00-56v5k4fqs2w3.janeway.replit.dev/api/"

    val api: PersonalDataService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PersonalDataService::class.java)
    }
}