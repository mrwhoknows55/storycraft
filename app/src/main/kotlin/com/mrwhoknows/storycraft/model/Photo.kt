package com.mrwhoknows.storycraft.model

import android.graphics.Bitmap
import android.net.Uri

data class Photo(
    val uri: Uri, val bitmap: Bitmap?
)

sealed class PhotoState {
    data class Loading(val imageUri: Uri) : PhotoState()
    data class Success(val photo: Photo) : PhotoState()
    data class Error(val message: String) : PhotoState()
}