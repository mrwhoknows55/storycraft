package com.mrwhoknows.storycraft.ui.screen

import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrwhoknows.storycraft.model.CanvasAction
import com.mrwhoknows.storycraft.model.CanvasAction.AddImage
import com.mrwhoknows.storycraft.model.CanvasAction.AddSticker
import com.mrwhoknows.storycraft.model.CanvasAction.BeginNewStroke
import com.mrwhoknows.storycraft.model.CanvasAction.ChangeColor
import com.mrwhoknows.storycraft.model.CanvasAction.ClearCanvas
import com.mrwhoknows.storycraft.model.CanvasAction.CompleteStroke
import com.mrwhoknows.storycraft.model.CanvasAction.DiscardImage
import com.mrwhoknows.storycraft.model.CanvasAction.DrawStroke
import com.mrwhoknows.storycraft.model.CanvasAction.SelectImage
import com.mrwhoknows.storycraft.model.EditorState
import com.mrwhoknows.storycraft.model.StrokePath
import com.mrwhoknows.storycraft.util.getBitmapFromDrawableRes
import com.mrwhoknows.storycraft.util.getStickersDrawableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class EditorViewModel() : ViewModel() {
    private val _state = MutableStateFlow<EditorState>(EditorState.EmptyCanvas)
    val state = _state.asStateFlow<EditorState>()


    fun onAction(action: CanvasAction) {
        when (action) {
            is SelectImage -> setImageUri(action.uri)
            is AddImage -> setBitmap(action.bitmap)
            is AddSticker -> {
                addSticker(action.drawableId, action.resources)
            }

            is ChangeColor -> updateCurrentColor(action.selectedColor)
            is DrawStroke -> onDraw(offset = action.position)
            BeginNewStroke -> onStartStroke()
            CompleteStroke -> onCompleteStroke()
            ClearCanvas -> clearStrokes()
            DiscardImage -> clearCanvas()
        }
    }

    private fun addSticker(stickerId: Int, resources: Resources) = _state.update { state ->
        if (state !is EditorState.PhotoWithDrawing) return@update state
        state.copy(
            sticker = resources.getBitmapFromDrawableRes(stickerId)
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun onStartStroke() = _state.update {
        if (it !is EditorState.PhotoWithDrawing) return@update it
        it.copy(
            currentStroke = StrokePath(
                id = Uuid.random().toString(), color = it.currentColor, path = emptyList()
            )
        )
    }

    private fun onDraw(offset: Offset) = _state.update { state ->
        if (state !is EditorState.PhotoWithDrawing) return@update state
        val currentStroke = state.currentStroke ?: return
        state.copy(
            currentStroke = currentStroke.copy(
                path = currentStroke.path + offset
            )
        )
    }

    private fun onCompleteStroke() = _state.update {
        if (it !is EditorState.PhotoWithDrawing) return@update it
        val currentStroke = it.currentStroke ?: return
        it.copy(
            currentStroke = null, allStrokePaths = it.allStrokePaths + currentStroke
        )
    }

    fun setImageUri(uri: Uri) {
        Timber.i("setImageUri: $uri")
        _state.update { EditorState.PhotoPicked(uri) }
    }

    private fun setBitmap(bitmap: Bitmap) {
        Timber.i("setBitmap: $bitmap")
        _state.update { state ->
            when (state) {
                is EditorState.EmptyCanvas -> {
                    EditorState.PhotoWithDrawing(mainPhoto = bitmap)
                }

                is EditorState.PhotoWithDrawing -> {
                    state.copy(mainPhoto = bitmap)
                }

                is EditorState.PhotoPicked -> {
                    EditorState.PhotoWithDrawing(mainPhoto = bitmap)
                }
            }
        }
    }

    private fun updateCurrentColor(color: Color) = _state.update { state ->
        when (state) {
            is EditorState.PhotoWithDrawing -> {
                state.copy(currentColor = color)
            }

            else -> {
                state
            }
        }
    }

    fun getBitmap(): Bitmap? = when (val state = _state.value) {
        is EditorState.PhotoWithDrawing -> {
            state.mainPhoto
        }

        else -> {
            null
        }
    }

    private fun clearStrokes() = getBitmap()?.let {
        clearCanvas()
        _state.update { state ->
            if (state !is EditorState.PhotoWithDrawing) return@update state
            state.copy(sticker = null)
        }
        setBitmap(it)
    }

    private fun clearCanvas() = _state.update { EditorState.EmptyCanvas }


    private val _stickers = MutableStateFlow<List<Int>>(emptyList())
    val stickers = _stickers.asStateFlow()

    init {
        viewModelScope.launch {
            _stickers.emit(getStickersDrawableList())
        }
    }
}