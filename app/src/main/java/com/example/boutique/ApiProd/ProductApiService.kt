package com.example.boutique.ApiProd

import com.example.boutique.ApiAuth.LoginRequest
import com.example.boutique.ApiAuth.Token
import com.example.boutique.ApiProd.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>

    @GET("tienda/product/findAll")
    suspend fun findAll(): Response<List<Product>>

    @GET("tienda/product/fetchByIdWithImage/{id}")
    suspend fun getProductWithImg(@Path("id") productId: Int): Response<ProductWithImg>

}