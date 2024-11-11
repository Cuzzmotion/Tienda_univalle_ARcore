package com.example.boutique.ApiAuth

data class PersonalData(
    val idpersonal_data: Int,
    val user_iduser: Int,
    val name: String,
    val lastname: String,
    val bank_account: String,
    val phone: String,
    val address: String,
    val is_deleted: Int,
    val password: String
)