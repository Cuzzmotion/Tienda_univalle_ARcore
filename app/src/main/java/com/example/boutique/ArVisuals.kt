package com.example.boutique

import android.content.Intent
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
        setContent {
            BoutiqueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val currentModel = remember { mutableStateOf("sports_bag") }
                        ARScreen(model = currentModel.value) {
                            finish()  // Finaliza este Activity y vuelve al anterior
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
        Food("sports_bag", R.drawable.bolso),
        Food("mochila", R.drawable.rojomochila),
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
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24), contentDescription = "previous")
        }
        CircularImage(imageId = itemsList[currentIndex].imageId)
        IconButton(onClick = { updateIndex(1) }) {
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), contentDescription = "next")
        }
    }
}

@Composable
fun CircularImage(modifier: Modifier = Modifier, imageId: Int) {
    Box(
        modifier = modifier
            .size(140.dp)
            .clip(CircleShape)
            .border(width = 3.dp, Translucent, CircleShape)
    ) {
        Image(painter = painterResource(id = imageId), contentDescription = null, modifier = Modifier.size(140.dp), contentScale = ContentScale.FillBounds)
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

        // Botón para colocar el objeto
        if (placeModelButton.value) {
            Button(
                onClick = {
                    modelNode.value?.anchor()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(text = "Colocar objeto")
            }
        }

        // Slider para ajustar la escala del modelo
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Escala del modelo: ${String.format("%.1f", scale)}")
            Slider(
                value = scale,
                onValueChange = { newScale ->
                    scale = newScale.coerceIn(0.5f, 2.0f)  // Límite entre 0.5 y 2.0
                    modelNode.value?.scale = Float3(scale, scale, scale) // Ajuste de tipo Float3
                },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // Botón para volver al Activity anterior
        Button(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text("Volver")
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

data class Food(var name: String, var imageId: Int)
}
