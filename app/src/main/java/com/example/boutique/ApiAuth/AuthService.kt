package com.example.boutique.ApiAuth

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PersonalDataService {
    @GET("personal_data")
    suspend fun getPersonalData(): Response<List<PersonalData>>
}