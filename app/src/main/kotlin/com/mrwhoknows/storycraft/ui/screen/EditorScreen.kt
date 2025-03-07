package com.mrwhoknows.storycraft.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mrwhoknows.storycraft.R
import com.mrwhoknows.storycraft.model.CanvasAction
import com.mrwhoknows.storycraft.model.EditorState
import com.mrwhoknows.storycraft.model.strokeColors
import com.mrwhoknows.storycraft.ui.componenet.DrawingCanvas
import com.mrwhoknows.storycraft.util.getImageBitmap
import com.mrwhoknows.storycraft.util.launchCamera
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    photoState: EditorState,
    stickers: List<Int>,
    onAction: (CanvasAction) -> Unit,
    onStoryShareClick: (Uri) -> Unit,
) {
    val background = colorScheme.background
    val context = LocalContext.current
    var saveCanvasAsImg by remember { mutableStateOf(false) }
    var showStickerPicker by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Timber.i("takePictureLauncher: $success, state: $photoState")
        if (success) {
            try {
                when (val state = photoState) {

                    is EditorState.PhotoPicked -> {
                        val capturedBitmap = context.getImageBitmap(state.uri)
                        onAction(CanvasAction.AddImage(capturedBitmap))
                    }

                    else -> {
                        Timber.e("PhotoState: $state")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading captured image")
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launchCamera(context) {
                onAction(CanvasAction.SelectImage(it!!))
            }
        } else {
            // todo handle permission denied
            Timber.e("requestPermissionLauncher: Camera permission denied")
        }
    }

    fun Context.checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePictureLauncher.launchCamera(context) {
                    Timber.i("checkCameraPermissionAndLaunch: photoUri: $it")
                    onAction(CanvasAction.SelectImage(it!!))
                }
            }
            // todo handle permission rationale

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier
            .background(background)
            .systemBarsPadding()
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .fillMaxSize()
            .padding(2.dp),
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f, fill = false)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            maxLines = 2
        ) {
            if (photoState !is EditorState.PhotoWithDrawing) {
                Button(onClick = context::checkCameraPermissionAndLaunch) {
                    Text(stringResource(R.string.capture_image))
                }
            } else {
                if (photoState.allStrokePaths.isNotEmpty()) {
                    Button(
                        onClick = {
                            onAction(CanvasAction.ClearCanvas)
                        },
                    ) {
                        Text(stringResource(R.string.clear_drawing))
                    }
                } else {
                    Button(
                        onClick = {
                            onAction(CanvasAction.DiscardImage)
                        },
                    ) {
                        Text(stringResource(R.string.discard_image))
                    }
                }

                Button(onClick = {
                    showStickerPicker = true
                }) {
                    Text(stringResource(R.string.add_sticker))
                }


                Button(onClick = {
                    saveCanvasAsImg = true
                }) {
                    Text(stringResource(R.string.share_on_ig_story))
                }

            }

        }

        val image = (photoState as? EditorState.PhotoWithDrawing)?.mainPhoto
        val sticker = (photoState as? EditorState.PhotoWithDrawing)?.sticker
        val currentPath = (photoState as? EditorState.PhotoWithDrawing)?.currentStroke
        val paths = (photoState as? EditorState.PhotoWithDrawing)?.allStrokePaths ?: emptyList()
        DrawingCanvas(modifier = Modifier
            .background(background)
            .weight(5f, fill = false)
            .fillMaxWidth()
            .padding(8.dp),
            photo = image,
            sticker = sticker,
            currentStroke = currentPath,
            paths = paths,
            action = onAction,
            saveToDisk = saveCanvasAsImg, onSaveDone = { uri ->
                saveCanvasAsImg = false
                onStoryShareClick(uri)
            })

        if (photoState is EditorState.PhotoWithDrawing) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 10.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                strokeColors.forEach { color ->
                    val currentColor = photoState.currentColor

                    if (color.value != currentColor.value) {
                        Box(modifier = Modifier
                            .size(40.dp)
                            .background(color, shape = CircleShape)
                            .clickable(true) {
                                onAction(CanvasAction.ChangeColor(color))
                            })
                    } else {
                        val borderColor = if (color !in listOf(Color.Red, Color.Magenta)) {
                            Color.Red
                        } else {
                            Color.Green
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(
                                    width = 2.dp,
                                    color = borderColor,
                                    shape = CircleShape,
                                )
                                .background(color, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    fun hide() {
        scope.launch {
            sheetState.hide()
            showStickerPicker = false
        }
    }
    if (showStickerPicker) {
        StickerPickerSheet(
            stickers = stickers,
            sheetState = sheetState,
            selectedStickerId = {
                onAction(CanvasAction.AddSticker(it, context.applicationContext.resources))
                hide()
            },
            dismiss = {
                hide()
            }
        )
    }
}



