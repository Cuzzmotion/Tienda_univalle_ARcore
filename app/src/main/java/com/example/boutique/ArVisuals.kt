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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
                        ARScreen(model = currentModel.value) {
                            finish()
                        }
                        Menu(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            onClick = { currentModel.value = it }
                        )
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
fun Menu(modifier: Modifier, onClick: (String) -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    val itemsList = listOf(
        ModelClass("bolsaArreglada_1111011409", R.drawable.bolsouvalle),
        ModelClass("JacketUvallefinal_1111032647", R.drawable.chaquetatrans),
        ModelClass("chaquetaAzulArreglada_1111032527", R.drawable.chaquetauvalle),
        ModelClass("neonpolera_1111032710", R.drawable.polerafosfo),
        ModelClass("Polo_Shirt_with_Unive_1111032633", R.drawable.polirauvalle),
        ModelClass( "Poloverde", R.drawable.poloverde),
        ModelClass("MochilaU", R.drawable.mochilaazul),
        ModelClass("MochilanoseU", R.drawable.mochilapeqverde)
    )
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
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24), contentDescription = "previous", tint = Color.White)
        }
        CircularImage(imageId = itemsList[currentIndex].imageId)
        IconButton(onClick = { updateIndex(1) }) {
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), contentDescription = "next", tint = Color.White)
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
        Image(painter = painterResource(id = imageId), contentDescription = null, modifier = Modifier.size(120.dp), contentScale = ContentScale.FillBounds)
    }
}

@Composable
fun ARScreen(model: String, onBack: () -> Unit) {
    val nodes = remember { mutableListOf<ArNode>() }
    val modelNode = remember { mutableStateOf<ArModelNode?>(null) }
    val placeModelButton = remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(0.8f) }  // Tamaño inicial del modelo

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false

                modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = scale  // Configuración de escala inicial
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp,5.dp,16.dp,5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, // Espaciado uniforme
            verticalAlignment = Alignment.CenterVertically
        ){
            // Botón para volver al Activity anterior
            Button(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp,5.dp,16.dp,5.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(183, 21, 54, 255),
                    contentColor = Color.White
                )
            ) {
                Text("Volver")
            }
            // Botón para colocar el objeto
            if (placeModelButton.value) {
                Button(
                    onClick = {
                        modelNode.value?.anchor()
                    },
                    modifier = Modifier
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(183, 21, 54, 255),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Fijar objeto")
                }
            }
        }
        // Slider para ajustar la escala del modelo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Escala del modelo: ${String.format("%.1f", scale)}",
                color = Color.White
            )
            Slider(
                value = scale,
                onValueChange = { newScale ->
                    scale = newScale.coerceIn(0.5f, 2.0f)  // Límite entre 0.5 y 2.0
                    modelNode.value?.scale = Float3(scale, scale, scale) // Ajuste de tipo Float3
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

    LaunchedEffect(key1 = model) {
        modelNode.value?.loadModelGlbAsync(
            glbFileLocation = "models/${model}.glb",
            scaleToUnits = scale
        )
        Log.e("errorloading", "ERROR LOADING MODEL")
    }
}

data class ModelClass(var name: String, var imageId: Int)
}
