package com.example.boutique.Apistest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://649d9368-3ac7-44de-bc74-c3e0315c25b8-00-56v5k4fqs2w3.janeway.replit.dev/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
