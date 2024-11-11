package com.example.boutique.ApiProd

import com.example.boutique.ApiProd.Product
import com.example.boutique.ApiProd.RetrofitInstanceProd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    suspend fun fetchProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            val response = RetrofitInstanceProd.api.getProducts()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
}