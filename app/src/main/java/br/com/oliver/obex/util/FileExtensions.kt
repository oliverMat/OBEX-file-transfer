package br.com.oliver.obex.util

import android.content.Context
import android.net.Uri
import java.io.File

fun Uri.uriToFile(context: Context): File? {
    val fileName = getFileNameFromUri(context, this) ?: return null

    val tempFile = File(context.cacheDir, fileName)
    try {
        context.contentResolver.openInputStream(this)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                name = it.getString(index)
            }
        }
    }
    return name
}