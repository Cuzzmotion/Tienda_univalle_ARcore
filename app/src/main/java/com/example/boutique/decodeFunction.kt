import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun decodeBase64Image(base64Image: String): Bitmap? {
    return try {
        // Paso 1: Eliminar el prefijo
        val base64String = base64Image.substringAfter("base64,")

        // Paso 2: Decodificar Base64 a un arreglo de bytes
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)

        // Paso 3: Convertir los bytes en un Bitmap
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null  // Devuelve null si ocurre un error en la decodificación
    }
}

