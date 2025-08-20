package com.example.boutique

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import com.example.boutique.ui.theme.BoutiqueTheme
import com.example.boutique.ui.theme.Translucent
import dev.romainguy.kotlin.math.Float3

class ArVisuals : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedModel = intent.getStringExtra("selectedModel") ?: "sports_bag"
        setContent {
            BoutiqueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val currentModel = remember { mutableStateOf(selectedModel) }
                        println("Current: "+currentModel.value)
                        ARScreen(model = currentModel.value, onModelChange = { newModel ->
                            currentModel.value = newModel
                        }) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BackButton() {
        Button(
            onClick = { finish() },
            modifier = Modifier.border(width = 20.dp, color = Translucent, shape = CircleShape)
        ) {
            Text(text = "Volver")
        }
    }
}

@Composable
fun Menu(modifier: Modifier, currentModelName: String, onClick: (String) -> Unit) {
    val itemsList = listOf(
        ModelClass("bolsaArreglada_1111011409", R.drawable.bolsouvalle),
        ModelClass("JacketUvallefinal_1111032647", R.drawable.chaquetatrans),
        ModelClass("chaquetaAzulArreglada_1111032527", R.drawable.chaquetauvalle),
        ModelClass("neonpolera_1111032710", R.drawable.polerafosfo),
        ModelClass("Polo_Shirt_with_Unive_1111032633", R.drawable.poloplomo),
        ModelClass("Poloverde", R.drawable.poloverde),
        ModelClass("MochilaU", R.drawable.mochilaazul),
        ModelClass("MochilanoseU", R.drawable.mochilabandolera)
    )

    // Encontrar el índice actual basado en el nombre del modelo
    var currentIndex by remember(currentModelName) {
        mutableStateOf(itemsList.indexOfFirst { it.name == currentModelName }.takeIf { it >= 0 } ?: 0)
    }

    fun updateIndex(offset: Int) {
        currentIndex = (currentIndex + offset + itemsList.size) % itemsList.size
        onClick(itemsList[currentIndex].name)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(onClick = { updateIndex(-1) }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                contentDescription = "Modelo anterior",
                tint = Color.White
            )
        }
        CircularImage(imageId = itemsList[currentIndex].imageId)
        IconButton(onClick = { updateIndex(1) }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                contentDescription = "Siguiente modelo",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CircularImage(modifier: Modifier = Modifier, imageId: Int) {
    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .border(width = 3.dp, color = Color.White, CircleShape)
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun ARScreen(model: String, onModelChange: (String) -> Unit, onBack: () -> Unit) {
    val nodes = remember { mutableListOf<ArNode>() }
    val modelNode = remember { mutableStateOf<ArModelNode?>(null) }
    val placeModelButton = remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(0.8f) }
    var showModelSelector by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize().testTag("ARContainer")
                .semantics { contentDescription = "Contenedor de AR" },
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false

                modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = scale
                    )
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = { node, _ ->
                        placeModelButton.value = node.isTracking
                    }
                }
                nodes.add(modelNode.value!!)
            },
            onSessionCreate = {
                planeRenderer.isVisible = false
            }
        )

        // Barra superior con botones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp, 5.dp, 16.dp, 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para volver
            Button(
                onClick = onBack,
                modifier = Modifier
                    .testTag("btnVolver")
                    .semantics { contentDescription = "Botón Volver" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(183, 21, 54, 255),
                    contentColor = Color.White
                )
            ) {
                Text(modifier = Modifier.testTag("txtBtnVolver"), text = "Volver")
            }

            // Botón para mostrar/ocultar selector de modelos
            Button(
                onClick = { showModelSelector = !showModelSelector },
                modifier = Modifier
                    .testTag("btnSelector")
                    .semantics { contentDescription = "Botón Selector de Modelos" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(183, 21, 54, 255),
                    contentColor = Color.White
                )
            ) {
                Text(
                    modifier = Modifier.testTag("txtBtnSelector"),
                    text = if (showModelSelector) "Ocultar" else "Modelos"
                )
            }
        }

        // Selector de modelos (se muestra/oculta según showModelSelector)
        if (showModelSelector) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                backgroundColor = Color.Black.copy(alpha = 0.7f),
                elevation = 8.dp
            ) {
                Menu(
                    modifier = Modifier.padding(16.dp),
                    currentModelName = model,
                    onClick = { newModel ->
                        onModelChange(newModel)
                        showModelSelector = false // Ocultar selector después de seleccionar
                    }
                )
            }
        }

        // Botones inferiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp, 5.dp, 16.dp, 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para colocar el objeto
            if (placeModelButton.value) {
                Button(
                    onClick = {
                        modelNode.value?.anchor()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("btnFijar")
                        .semantics { contentDescription = "btnFijarObjeto" },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(183, 21, 54, 255),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        modifier = Modifier
                            .testTag("txtBtnFijar")
                            .semantics { contentDescription = "Texto BtnFijarObj" },
                        text = "Fijar objeto"
                    )
                }
            }
        }

        // Slider para ajustar la escala del modelo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .testTag("escalaSliderModelo")
                    .semantics { contentDescription = "Escala del Modelo" },
                text = "Escala del modelo: ${String.format("%.1f", scale)}",
                color = Color.White
            )
            Slider(
                value = scale,
                onValueChange = { newScale ->
                    scale = newScale.coerceIn(0.5f, 2.0f)
                    modelNode.value?.scale = Float3(scale, scale, scale)
                },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.padding(horizontal = 32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(183, 21, 54, 255),
                    activeTrackColor = Color(183, 21, 54, 255),
                    inactiveTrackColor = Color.Gray,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )
        }
    }

    // Efecto para cargar nuevo modelo cuando cambia
    LaunchedEffect(key1 = model) {
        modelNode.value?.loadModelGlbAsync(
            glbFileLocation = "models/${model}.glb",
            scaleToUnits = scale
        )
        Log.d("ModelChange", "Cargando modelo: $model")
    }
}

data class ModelClass(var name: String, var imageId: Int)