package com.mrwhoknows.storycraft.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "IMG_${timeStamp}_"
    val storageDir = getExternalFilesDir(null)
    return File.createTempFile(
        imageFileName, ".jpg", storageDir
    )
}

fun ManagedActivityResultLauncher<Uri, Boolean>.launchCamera(
    context: Context, uri: (Uri?) -> Unit
) {
    val photoFile = context.createImageFile()
    val photoUri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", photoFile
    )
    uri(photoUri)
    launch(photoUri)
}

fun Context.getImageBitmap(imageUri: Uri): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }


fun ContentResolver.getUriFromBitmap(bitmap: Bitmap): Uri? {
    val title = "IMG_" + System.currentTimeMillis().toString()
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$title.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DESCRIPTION, "Image captured by MyApp")
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(
            MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp"
        ) // Save in Pictures/MyApp folder
    }
    val imageUri: Uri? = this.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    imageUri?.let { uri ->
        openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
    }
    return imageUri
}

fun Context.shareOnIGStory(bitmap: Bitmap) {
    val uri = contentResolver.getUriFromBitmap(bitmap) ?: return
    val storiesIntent = Intent("com.instagram.share.ADD_TO_STORY")
    storiesIntent.setDataAndType(uri, "image/*")
    storiesIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    storiesIntent.setPackage("com.instagram.android")
    grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(storiesIntent)
}

suspend fun Bitmap.saveToDisk(context: Context): Uri = withContext(Dispatchers.IO) {
    val file = context.createImageFile()
    val photoUri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", file
    )

    file.writeBitmap(this@saveToDisk, Bitmap.CompressFormat.PNG, 100)
    Timber.d("Saved to disk: $photoUri")

    return@withContext photoUri
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}