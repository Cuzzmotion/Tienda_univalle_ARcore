package com.example.boutique

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.boutique.Apistest.Bill
import com.example.boutique.Apistest.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Testapi : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BillListScreen()
        }
    }
}

@Composable
fun BillListScreen() {
    var bills by remember { mutableStateOf<List<Bill>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Llamada a la API
    LaunchedEffect(Unit) {
        RetrofitInstance.api.getBills().enqueue(object : Callback<List<Bill>> {
            override fun onResponse(call: Call<List<Bill>>, response: Response<List<Bill>>) {
                if (response.isSuccessful) {
                    bills = response.body()
                } else {
                    errorMessage = "Error en la respuesta: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Bill>>, t: Throwable) {
                errorMessage = "Error en la llamada: ${t.message}"
            }
        })
    }

    // Interfaz de usuario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text("Error: $errorMessage")
        } else if (bills != null) {
            BillList(bills!!)
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun BillList(bills: List<Bill>) {
    Column {
        bills.forEach { bill ->
            BillItem(bill)
            Divider()
        }
    }
}

@Composable
fun BillItem(bill: Bill) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("ID: ${bill.idbills}", style = MaterialTheme.typography.h6)
        Text("Sale ID: ${bill.sales_idsales}")
        Text("Total Price: ${bill.total_price}")
        Text("Is Deleted: ${bill.is_deleted}")
    }
}