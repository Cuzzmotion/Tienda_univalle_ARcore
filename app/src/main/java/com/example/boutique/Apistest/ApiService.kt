package com.example.boutique.Apistest

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("bills")
    fun getBills(): Call<List<Bill>>
}