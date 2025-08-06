package edu.ucne.recrearte.presentation.work
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream



object ImageUtils {
    fun Uri.toFile(context: Context): File {
        // Create a temporary file
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)

        context.contentResolver.openInputStream(this)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }
}