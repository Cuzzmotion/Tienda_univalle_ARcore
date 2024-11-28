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
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.boutique.ApiAuth.LoginRequest
import com.example.boutique.ApiAuth.RetrofitInstanceAuthJwt
import com.example.boutique.ApiProd.Product
import com.example.boutique.ApiProd.ProductRepository
import com.example.boutique.ApiProd.ProductWithImg
import decodeBase64Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow


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

@OptIn(ExperimentalMaterial3Api::class)
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
            modifier = Modifier.fillMaxWidth().testTag("LoginButton").semantics {
                contentDescription = "Logo de Univalle"
            }

        )
        Text(text = "Univalle Boutique", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Inicia Sesión", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(90.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth().testTag("inputUser").semantics {
                contentDescription = "Input para el nombre de usuario"
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(183, 21, 54, 255),
                focusedLabelColor = Color.Black,
                cursorColor = Color(98, 95, 95, 230)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = contraseña,
            onValueChange = { contraseña = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth().testTag("inputPassword").semantics {
                contentDescription = "Input para la contraseña"
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(183, 21, 54, 255),
                focusedLabelColor = Color.Black,
                cursorColor = Color(98, 95, 95, 230)
            )
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp).testTag("messageError").semantics {
                contentDescription = "Espacio para mostrar mensaje de error"
            })
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
                .height(48.dp).testTag("btnSubmit").semantics {
                    contentDescription = "Boton de ingreso"
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color(183, 21, 54, 255))
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth().testTag("btnSubmitWithoutUser").semantics {
                contentDescription = "Boton ingresar sin cuenta"
            }
        ) {
            Text("Ingresar sin cuenta", color = Color(183, 21, 54, 255))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
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
        println("Aqui: \n"+productos)
    }

    // Conjuntos de imágenes para cada opción
    val popularImages = listOf(R.drawable.chaquetatrans, R.drawable.chaquetauvalle)
    val nuevoImages = listOf(R.drawable.mochilabandolera, R.drawable.mochilaazul)
    val recomendadoImages = listOf(R.drawable.poloverde, R.drawable.polerafosfo)


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
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color(183, 21, 54, 255)
                    ),
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
                        .padding(horizontal = 4.dp).testTag("buttonsNevigation").semantics {
                            contentDescription = "Botones de navegacion"
                        }
                ) {
                    Text(option)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Opciones seleccionables ("Popular", "Nuevo", "Recomendado") centradas y estilizadas


        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar las imágenes filtradas (según búsqueda) o las correspondientes a la opción seleccionada
        if (isSearching && searchText.isNotEmpty()) {
            // Mostrar resultados de búsqueda
            Text("Resultados de búsqueda para \"$searchText\":", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Si hay productos, muestra la lista
                items(filteredProducts) { product ->
                    val repository = ProductRepository()
                    val decodedImage = remember { mutableStateOf<Bitmap?>(null) }
                    // Lógica para obtener la imagen del producto
                    LaunchedEffect(product.idproducts) {
                        val result = repository.getProductWithImg(product.idproducts)
                        if (result != null) {
                            decodedImage.value = decodeBase64Image(result.image)
                        }
                    }
                    // Fila de producto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color(0xFFF5F5F5), MaterialTheme.shapes.small) // Fondo claro
                            .padding(16.dp) // Espaciado interior
                            .clickable {
                                navController.navigate("productDetail/${product.idproducts}")
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Información del producto
                        Column(
                            modifier = Modifier.weight(1f) // Para que la columna ocupe el espacio disponible
                        ) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp)) // Espacio entre los textos
                            Text(
                                text = product.unitPrice,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Imagen del producto (si está disponible)
                        decodedImage.value?.let { bitmap ->
                            Image(
                                painter = remember { BitmapPainter(bitmap.asImageBitmap()) },
                                contentDescription = product.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                                    .background(Color.White, MaterialTheme.shapes.small)
                            )
                        } ?: run {
                            Text("Imagen...")
                        }
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
            TextButton(onClick = { /* Acción para Ver todo */ },
                modifier = Modifier.
                testTag("btnShowAll")
                .semantics {
                contentDescription = "Boton para mostrar todos los items"
                }
            ) {
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
                        Text("Imagen...")
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
            Text("Cargando imagen..")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del producto y categoría
        Text(text = productWithImg?.product?.name ?: "Nombre del Producto", style = MaterialTheme.typography.titleLarge)
        Text(text = "Prenda", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones de tallas
        Text(text = "Tallas", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf("S", "M", "L", "XL").forEach { size ->
                Button(
                    onClick = { selectedSize = size },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSize == size) Color(183, 21, 54, 255) else Color.Transparent,
                        contentColor = if (selectedSize == size) Color.White else Color(183, 21, 54, 255)
                    ),
                    border = BorderStroke(1.dp, Color(183, 21, 54, 255)),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(40.dp)
                ) {
                    Text(size)
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        // Precio
        Text(text = "Precio", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = ("Bs." + productWithImg?.product?.unitPrice), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.weight(1f))

        // Botón de visualización
        val context = LocalContext.current
        Button(
            onClick = {
                var selectedModelName: String? = ""
                when (productName) {
                    13-> selectedModelName = "JacketUvallefinal_1111032647"
                    14 -> selectedModelName = "bolsaArreglada_1111011409"
                    36 -> selectedModelName = "chaquetaAzulArreglada_1111032527"
                    37 -> selectedModelName = "MochilaU"
                    38 -> selectedModelName = "Poloverde"
                    39 -> selectedModelName = "Polo_Shirt_with_Unive_1111032633"
                    40 -> selectedModelName = "neonpolera_1111032710"
                    41 -> selectedModelName = "MochilanoseU"
                    else -> println("Modelo no encontrado")
                }
                val intent = Intent(context, ArVisuals::class.java)
                intent.putExtra("selectedModel", selectedModelName)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(183, 21, 54, 255))
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