package com.example.boutique.ApiAuth

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val email: String, val pass: String)
data class Token(val access_token: String)

interface PersonalDataService {
    @GET("personal_data")
    suspend fun getPersonalData(): Response<List<PersonalData>>

    @Headers("ngrok-skip-browser-warning: true")
    @POST("tienda/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Token>
}