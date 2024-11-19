package com.example.boutique


import TokenManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.boutique.ApiAuth.LoginRequest
import com.example.boutique.ApiAuth.RetrofitInstanceAuth
import com.example.boutique.ApiAuth.RetrofitInstanceAuthJwt
import com.example.boutique.ApiAuth.Token
import com.example.boutique.ApiProd.Product
import com.example.boutique.ApiProd.ProductApiService
import com.example.boutique.ApiProd.ProductRepository
import com.example.boutique.ApiProd.ProductWithImg
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import decodeBase64Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("productDetail/{productName}") { backStackEntry ->
                        val productName = backStackEntry.arguments?.getString("productName")

                        val product = productName?.toInt()

                        ProductDetailScreen(navController = navController, productName = product)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var usuario by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "LogoUnivalle",
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Univalle Boutique", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Inicia Sesión", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(90.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(90.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val loginRequest = LoginRequest(usuario, contraseña)
                    try {
                        val responsejwt = RetrofitInstanceAuthJwt.api.login(loginRequest);
                        println(responsejwt);
                        TokenManager.jwtToken = responsejwt.body()?.access_token
                        println("hola");
                        if (responsejwt.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                navController.navigate("home")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Credenciales Incorrectas"
                            }
                        }
                    } catch(e: Exception){
                        println(e.message);
                        println("Murio");
                    }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(183, 21, 54, 255))
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar sin cuenta", color = Color(183, 21, 54, 255))
        }
    }
}


@Composable
fun HomeScreen(navController: NavController) {
    var selectedOption by remember { mutableStateOf("Popular") }
    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Llamar a la API para obtener los productos
    LaunchedEffect(Unit) {
        val repository = ProductRepository()
        productos = repository.fetchAll()
        println(productos)
    }

    // Conjuntos de imágenes para cada opción
    val popularImages = listOf(R.drawable.buzo, R.drawable.cafe)
    val nuevoImages = listOf(R.drawable.chaqueta, R.drawable.canguro)
    val recomendadoImages = listOf(R.drawable.canguro, R.drawable.buzo)

    // Elegir imágenes según la opción seleccionada
    val imagesToShow = when (selectedOption) {
        "Popular" -> popularImages
        "Nuevo" -> nuevoImages
        "Recomendado" -> recomendadoImages
        else -> popularImages
    }

    // Filtrar productos según el término de búsqueda
    val filteredProducts = if (isSearching && searchText.isNotEmpty()) {
        productos.filter { it.name.contains(searchText, ignoreCase = true) }
    } else {
        productos
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra superior con ícono de búsqueda o campo de búsqueda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearching) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Buscar...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            searchText = ""
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                        }
                    }
                )
            } else {
                Text(text = "Univalle Boutique", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { isSearching = true }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf("Popular", "Nuevo", "Recomendado").forEach { option ->
                Button(
                    onClick = { selectedOption = option },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedOption == option) Color(183, 21, 54, 255) else Color.Transparent,
                        contentColor = if (selectedOption == option) Color.White else Color(183, 21, 54, 255)
                    ),
                    border = BorderStroke(1.dp, Color(183, 21, 54, 255)),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 100.dp)
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(option)
                }
            }
        }
        // Imágenes debajo de los botones de opciones
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = R.drawable.buzo),
                contentDescription = "Imagen Popular",
                modifier = Modifier.size(60.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.cafe),
                contentDescription = "Imagen Nuevo",
                modifier = Modifier.size(60.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.chaqueta),
                contentDescription = "Imagen Recomendado",
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Opciones seleccionables ("Popular", "Nuevo", "Recomendado") centradas y estilizadas


        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar las imágenes filtradas (según búsqueda) o las correspondientes a la opción seleccionada
        if (isSearching && searchText.isNotEmpty()) {
            // Mostrar resultados de búsqueda
            Text("Resultados de búsqueda para \"$searchText\":", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                filteredProducts.forEach { product ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(product.name, style = MaterialTheme.typography.bodyMedium)
                        Text(product.unitPrice, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // Muestra las imágenes según la opción seleccionada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                imagesToShow.forEach { imageRes ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(4.dp)
                            .background(Color.LightGray, MaterialTheme.shapes.small)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección "Recomendado" con "Ver todo" encima de la lista de productos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recomendado", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { /* Acción para Ver todo */ }) {
                Text("Ver todo", color = Color(183, 21, 54, 255), style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de productos recomendados con navegación a ProductDetailScreen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
                // Si hay productos, muestra la lista
                items(productos) { product ->
                    val repository = ProductRepository()
                    val decodedImage = remember { mutableStateOf<Bitmap?>(null) }

                    product.idproducts
                    LaunchedEffect(product.idproducts) {
                        val result = repository.getProductWithImg(product.idproducts)
                        if (result != null) {
                            decodedImage.value = decodeBase64Image(result.image)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFFF5F5F5), MaterialTheme.shapes.small)
                            .padding(8.dp)
                            .clickable {
                                navController.navigate("productDetail/${product.idproducts}")
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.bodyLarge)
                            Text(product.unitPrice, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        decodedImage.value?.let { bitmap ->
                            Image(
                                painter = remember { BitmapPainter(bitmap.asImageBitmap()) }, // Convertir el Bitmap a ImageBitmap
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .background(Color.White, MaterialTheme.shapes.small)
                            )
                        } ?: run {
                            // Si no se ha decodificado la imagen, muestra una imagen por defecto
                            Image(
                                painter = painterResource(id = R.drawable.buzo),
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .background(Color.White, MaterialTheme.shapes.small)
                            )
                        }
                    }
                }

        }
    }
}


@Composable
fun ProductDetailScreen(navController: NavController, productName: Int?) {
    // Estado para los datos del producto
    var productWithImg by remember { mutableStateOf<ProductWithImg?>(null) }
    var selectedSize by remember { mutableStateOf("S") }
    val decodedImage = remember { mutableStateOf<Bitmap?>(null) }

    // Se realiza la carga de datos en LaunchedEffect
    LaunchedEffect(productName) {
        productName?.let {
            val result = ProductRepository().getProductWithImg(it)
            productWithImg = result
            decodedImage.value = result?.image?.let { it1 -> decodeBase64Image(it1) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra superior con botón de retroceso y menú de opciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Detalle del Producto", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Acción para mostrar más opciones */ }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Imagen del producto
        decodedImage.value?.let { bitmap ->
            Image(
                painter = remember { BitmapPainter(bitmap.asImageBitmap()) },
                contentDescription = productWithImg?.product?.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, MaterialTheme.shapes.small)
            )
        } ?: run {
            Image(
                painter = painterResource(id = R.drawable.buzo),
                contentDescription = productWithImg?.product?.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
                    .background(Color.White, MaterialTheme.shapes.small)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del producto y categoría
        Text(text = productWithImg?.product?.name ?: "Nombre del Producto", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Categoría", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones de tallas
        Text(text = "Tallas", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf("S", "M", "L", "XL").forEach { size ->
                Button(
                    onClick = { selectedSize = size },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSize == size) Color(0xFF2D2926) else Color.Transparent,
                        contentColor = if (selectedSize == size) Color.White else Color(0xFF8D6E63)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF8D6E63)),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(40.dp)
                ) {
                    Text(size)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción del producto
        Text(text = "Acerca del producto", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Detalles.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Precio
        Text(text = "Precio", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = ("Bs." + productWithImg?.product?.unitPrice), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.weight(1f))

        // Botón de visualización
        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent(context, ArVisuals::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Visualizar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Visualizar")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    MaterialTheme {
        ProductDetailScreen(navController = rememberNavController(), productName = 12)
    }
}



