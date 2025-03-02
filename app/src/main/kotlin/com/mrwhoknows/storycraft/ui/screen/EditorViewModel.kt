package com.mrwhoknows.storycraft.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.mrwhoknows.storycraft.model.CanvasAction
import com.mrwhoknows.storycraft.model.CanvasAction.AddImage
import com.mrwhoknows.storycraft.model.CanvasAction.AddPoint
import com.mrwhoknows.storycraft.model.CanvasAction.BeginStroke
import com.mrwhoknows.storycraft.model.CanvasAction.ChangeColor
import com.mrwhoknows.storycraft.model.CanvasAction.ClearCanvas
import com.mrwhoknows.storycraft.model.CanvasAction.CompleteStroke
import com.mrwhoknows.storycraft.model.CanvasAction.SelectImage
import com.mrwhoknows.storycraft.model.EditorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber


class EditorViewModel() : ViewModel() {
    private val _photo = MutableStateFlow<EditorState>(EditorState.EmptyCanvas)
    val photo = _photo.asStateFlow<EditorState>()


    fun onAction(action: CanvasAction) {
        when (action) {
            is SelectImage -> setImageUri(action.uri)
            is AddImage -> setBitmap(action.bitmap)
            is AddPoint -> TODO()
            is ChangeColor -> updateCurrentColor(action.selectedColor)
            ClearCanvas -> clearCanvas()
            BeginStroke -> TODO()
            CompleteStroke -> TODO()
        }
    }

    private fun setImageUri(uri: Uri) {
        Timber.i("setImageUri: $uri")
        _photo.update { EditorState.PhotoPicked(uri) }
    }

    private fun setBitmap(bitmap: Bitmap) {
        Timber.i("setBitmap: $bitmap")
        _photo.update { state ->
            when (state) {
                is EditorState.EmptyCanvas -> {
                    EditorState.PhotoWithDrawing(bitmap = bitmap)
                }

                is EditorState.PhotoWithDrawing -> {
                    state.copy(bitmap = bitmap)
                }

                is EditorState.PhotoPicked -> {
                    EditorState.PhotoWithDrawing(bitmap = bitmap)
                }
            }
        }
    }

    private fun updateCurrentColor(color: Color) = _photo.update { state ->
        when (state) {
            is EditorState.PhotoWithDrawing -> {
                state.copy(drawing = state.drawing.copy(currentColor = color))
            }

            else -> {
                state
            }
        }
    }

    fun getBitmap(): Bitmap? = when (val state = _photo.value) {
        is EditorState.PhotoWithDrawing -> {
            state.bitmap
        }

        else -> {
            null
        }
    }

    private fun clearCanvas() {
        _photo.update { EditorState.EmptyCanvas }
    }
}