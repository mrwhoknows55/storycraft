package com.mrwhoknows.storycraft.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.mrwhoknows.storycraft.model.Photo
import com.mrwhoknows.storycraft.model.PhotoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber


class EditorViewModel() : ViewModel() {
    private val _photo = MutableStateFlow<PhotoState>(PhotoState.Loading(Uri.EMPTY))
    val photo = _photo.asStateFlow<PhotoState>()

    fun setImageUri(uri: Uri) {
        Timber.i("setImageUri: $uri")
        _photo.update { PhotoState.Loading(uri) }
    }

    fun setBitmap(bitmap: Bitmap) {
        Timber.i("setBitmap: $bitmap")
        _photo.update { state ->
            when (state) {
                is PhotoState.Error -> {
                    PhotoState.Success(Photo(Uri.EMPTY, bitmap))
                }

                is PhotoState.Loading -> {
                    PhotoState.Success(Photo(state.imageUri, bitmap))
                }

                is PhotoState.Success -> {
                    PhotoState.Success(state.photo.copy(bitmap = bitmap))
                }
            }
        }
    }

    fun getBitmap(): Bitmap? = when (val state = _photo.value) {
        is PhotoState.Success -> {
            state.photo.bitmap
        }

        else -> {
            null
        }
    }

    fun clearCanvas() {
        _photo.update { PhotoState.Loading(Uri.EMPTY) }
    }
}