package com.mrwhoknows.storycraft.ui.componenet


import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.mrwhoknows.storycraft.model.CanvasAction
import com.mrwhoknows.storycraft.model.StrokePath
import com.mrwhoknows.storycraft.util.saveToDisk
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    photo: Bitmap? = null,
    sticker: Bitmap? = null,
    saveToDisk: Boolean = false,
    onSaveDone: (Uri) -> Unit,
    currentStroke: StrokePath? = null,
    paths: List<StrokePath> = emptyList(),
    action: (CanvasAction) -> Unit,
) {
    var canvasHeight by remember { mutableStateOf(200.dp) }
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var stickerOffset by remember { mutableStateOf(IntOffset(Int.MIN_VALUE, Int.MIN_VALUE)) }
    var stickerSize by remember { mutableStateOf(IntSize(0, 0)) }
    Canvas(modifier = modifier
        .height(canvasHeight)
        .clipToBounds()
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(onDrag = { change, dragAmount ->
                change.consume()
                stickerOffset = IntOffset(
                    stickerOffset.x + dragAmount.x.toInt(),
                    stickerOffset.y + dragAmount.y.toInt()
                )
            })
        }
        .drawWithCache {
            if (saveToDisk) {
                onDrawWithContent {
                    val graphicsLayer = this@drawWithCache.obtainGraphicsLayer()
                    graphicsLayer.record {
                        this@onDrawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)

                    coroutineScope.launch {
                        val bitmap = graphicsLayer.toImageBitmap()
                        val uri = bitmap.asAndroidBitmap().saveToDisk(context)
                        onSaveDone(uri)
                    }
                }
            } else {
                onDrawWithContent {
                    drawContent()
                }
            }
        }
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = {
                action(CanvasAction.BeginNewStroke)
            }, onDragEnd = {
                action(CanvasAction.CompleteStroke)
            }, onDrag = { change, _ ->
                action(CanvasAction.DrawStroke(change.position))
            }, onDragCancel = {
                action(CanvasAction.CompleteStroke)
            })
        }

    ) {

        photo?.asImageBitmap()?.let { imageBitmap ->
            canvasHeight = with(density) {
                imageBitmap.height.toDp()
            }
            val scale = minOf(
                size.width / imageBitmap.width, size.height / imageBitmap.height
            )
            val scaledWidth = imageBitmap.width * scale
            val scaledHeight = imageBitmap.height * scale

            drawImage(
                image = imageBitmap,
                dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt()),
                dstOffset = IntOffset(
                    ((size.width - scaledWidth) / 2).toInt(),
                    ((size.height - scaledHeight) / 2).toInt(),
                )
            )
        }

        sticker?.asImageBitmap()?.let { imageBitmap ->
            stickerSize = IntSize(imageBitmap.width, imageBitmap.height)

            if (stickerOffset.x == Int.MIN_VALUE && stickerOffset.y == Int.MIN_VALUE) {
                val offsetX = (size.width - stickerSize.width) / 2
                val offsetY = (size.height - stickerSize.height) / 2
                stickerOffset = IntOffset(offsetX.toInt(), offsetY.toInt())
            }
            drawImage(
                image = imageBitmap, dstSize = stickerSize, dstOffset = stickerOffset
            )
        }

        paths.fastForEach { stroke ->
            drawStroke(stroke)
        }

        currentStroke?.let { stroke ->
            drawStroke(stroke)
        }
    }
}


fun DrawScope.drawStroke(
    stroke: StrokePath
) {
    drawPath(
        path = Path().apply {
            stroke.path.fastForEachIndexed { index, offset ->
                if (index == 0) {
                    moveTo(offset.x, offset.y)
                } else {
                    lineTo(offset.x, offset.y)
                }
            }
        }, color = stroke.color, style = Stroke(
            width = stroke.thickness, cap = StrokeCap.Round, join = StrokeJoin.Round
        )
    )
}

