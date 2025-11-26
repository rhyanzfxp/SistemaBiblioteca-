package com.example.myapplication.core

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun Uri.toFile(context: Context): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(this) ?: "image/jpeg"
    val fileName = "cover_${System.currentTimeMillis()}.jpg"

    val tempFile = File(context.cacheDir, fileName)
    contentResolver.openInputStream(this)?.use { inputStream ->
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("cover", tempFile.name, requestBody)
}