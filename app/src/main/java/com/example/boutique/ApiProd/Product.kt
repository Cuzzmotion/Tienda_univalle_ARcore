package com.example.boutique.ApiProd

data class Product(
    val idproducts: Int,
    val name: String,
    val imageUrl: String,
    val unitPrice: String,
    val is_deleted: Int
)