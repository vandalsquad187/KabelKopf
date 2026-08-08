package com.docai.kabelkopf.util

import android.content.Context
import android.net.Uri
import java.io.File

object FileUtils {
    fun getPhotoDir(context: Context): File {
        val dir = File(context.filesDir, Constants.PHOTO_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getVoiceDir(context: Context): File {
        val dir = File(context.filesDir, Constants.VOICE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPdfDir(context: Context): File {
        val dir = File(context.filesDir, Constants.PDF_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createPhotoFile(context: Context): File {
        val dir = getPhotoDir(context)
        val name = "photo_${System.currentTimeMillis()}.jpg"
        return File(dir, name)
    }

    fun createVoiceFile(context: Context): File {
        val dir = getVoiceDir(context)
        val name = "voice_${System.currentTimeMillis()}.3gp"
        return File(dir, name)
    }

    fun getAssetPhotoDir(context: Context): File {
        val dir = File(context.filesDir, "assets/photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getAssetDocDir(context: Context): File {
        val dir = File(context.filesDir, "assets/docs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createAssetPhotoFile(context: Context): File {
        val dir = getAssetPhotoDir(context)
        val name = "asset_photo_${System.currentTimeMillis()}.jpg"
        return File(dir, name)
    }

    fun createAssetDocFile(context: Context, extension: String = "pdf"): File {
        val dir = getAssetDocDir(context)
        val name = "asset_doc_${System.currentTimeMillis()}.$extension"
        return File(dir, name)
    }

    fun copyUriToFile(context: Context, uri: Uri, targetFile: File): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return targetFile.absolutePath
    }
}
