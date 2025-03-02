package com.mrwhoknows.storycraft.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Drawing(
    val currentColor: Color = allColors.first(),
    val currentPath: DrawPath? = null,
    val paths: List<DrawPath> = emptyList()
)

sealed interface EditorState {
    object EmptyCanvas : EditorState
    data class PhotoPicked(val uri: Uri) : EditorState
    data class PhotoWithDrawing(
        val bitmap: Bitmap,
        val drawing: Drawing = Drawing(),
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

data class DrawPath(
    val id: String, val color: Color, val path: List<Offset>
)

sealed interface CanvasAction {
    data class SelectImage(val uri: Uri) : CanvasAction
    data class AddImage(val bitmap: Bitmap) : CanvasAction
    data object BeginStroke : CanvasAction
    data class AddPoint(val position: Offset) : CanvasAction
    data object CompleteStroke : CanvasAction
    data class ChangeColor(val selectedColor: Color) : CanvasAction
    data object ClearCanvas : CanvasAction
}