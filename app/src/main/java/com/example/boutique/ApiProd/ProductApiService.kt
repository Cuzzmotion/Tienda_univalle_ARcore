package com.example.boutique.ApiProd

import com.example.boutique.ApiProd.Product
import retrofit2.Response
import retrofit2.http.GET

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
}