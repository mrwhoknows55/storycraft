package com.mrwhoknows.storycraft.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


sealed interface EditorState {
    object EmptyCanvas : EditorState
    data class PhotoPicked(val uri: Uri) : EditorState
    data class PhotoWithDrawing(
        val bitmap: Bitmap,
        val currentColor: Color = allColors.first(),
        val currentStroke: StrokePath? = null,
        val allStrokePaths: List<StrokePath> = emptyList()
    ) : EditorState
}

val allColors = listOf(
    Color.LightGray,
    Color.Gray,
    Color.Black,
    Color.Yellow,
    Color.Blue,
    Color.Green,
    Color.Red,
    Color.Magenta
)

@OptIn(ExperimentalUuidApi::class)
data class StrokePath(
    val id: String = Uuid.random().toString(),
    val path: List<Offset>,
    val color: Color,
    val thickness: Float = 8f
)

sealed interface CanvasAction {
    data class SelectImage(val uri: Uri) : CanvasAction
    data class AddImage(val bitmap: Bitmap) : CanvasAction
    data class ChangeColor(val selectedColor: Color) : CanvasAction
    data class DrawStroke(val position: Offset) : CanvasAction
    data object BeginNewStroke : CanvasAction
    data object CompleteStroke : CanvasAction
    data object ClearCanvas : CanvasAction
    data object DiscardImage : CanvasAction
}