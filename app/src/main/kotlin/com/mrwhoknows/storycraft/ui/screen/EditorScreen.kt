package com.mrwhoknows.storycraft.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mrwhoknows.storycraft.R
import com.mrwhoknows.storycraft.model.PhotoState
import com.mrwhoknows.storycraft.util.getImageBitmap
import com.mrwhoknows.storycraft.util.launchCamera
import timber.log.Timber


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    photoState: PhotoState,
    setImageUri: (Uri) -> Unit,
    setBitmap: (Bitmap) -> Unit,
    onStoryShareClick: () -> Unit,
    onDiscardImageClick: () -> Unit
) {
    val background = colorScheme.background
    val context = LocalContext.current

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        Timber.i("takePictureLauncher: $success, state: $photoState")
        if (success) {
            try {
                when (val state = photoState) {
                    is PhotoState.Loading -> {
                        val capturedBitmap = context.getImageBitmap(state.imageUri)
                        setBitmap(capturedBitmap)
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
                setImageUri(it!!)
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
                    setImageUri(it!!)
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
                .weight(1f, fill = false)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            if (photoState !is PhotoState.Success) {
                Button(onClick = context::checkCameraPermissionAndLaunch) {
                    Text(stringResource(R.string.capture_image))
                }

            } else {
                Button(onClick = onDiscardImageClick) {
                    Text(stringResource(R.string.discard_image))
                }

                Button(onClick = onStoryShareClick) {
                    Text(stringResource(R.string.share_on_ig_story))
                }
            }
        }
        Canvas(
            modifier = Modifier
                .background(background)
                .weight(5f, fill = false)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            when (val state = photoState) {
                is PhotoState.Success -> {
                    state.photo.bitmap?.asImageBitmap()?.let { imageBitmap ->
                        Timber.i("Drawing image: ${imageBitmap.height} -> ${imageBitmap.width}")
                        val scale = minOf(
                            size.width / imageBitmap.width, size.height / imageBitmap.height
                        )
                        val scaledWidth = imageBitmap.width * scale
                        val scaledHeight = imageBitmap.height * scale
                        drawImage(
                            image = imageBitmap,
                            dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
                        )
                    } ?: run {
                        Timber.e("Error loading image: state: $photoState")
                    }
                }

                is PhotoState.Error -> {
                    // TODO()
                    drawRect(color = background, size = size)
                }

                is PhotoState.Loading -> {
                    // TODO()
                    drawRect(color = background, size = size)
                }
            }
        }
    }
}



