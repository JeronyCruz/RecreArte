package edu.ucne.recrearte.presentation.work

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

//object ImageUtils {
//    fun encodeImageToBase64(bitmap: Bitmap): String {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//        val byteArray = outputStream.toByteArray()
//        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
//    }
//}
object ImageUtils {
    fun encodeImageToBase64(bitmap: Bitmap): String {
        return try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Reducir calidad para ahorrar espacio
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error encoding image: ${e.message}")
            ""
        }
    }

    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error decoding image: ${e.message}")
            null
        }
    }
}