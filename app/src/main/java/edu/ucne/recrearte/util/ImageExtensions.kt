package edu.ucne.recrearte.util

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun ImageBitmap.Companion.imageFromBase64(base64: String): ImageBitmap {
    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return bitmap.asImageBitmap()
}