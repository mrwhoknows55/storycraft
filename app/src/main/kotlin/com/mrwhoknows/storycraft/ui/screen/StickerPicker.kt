package com.mrwhoknows.storycraft.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    stickers: List<Int>,
    sheetState: SheetState,
    selectedStickerId: (Int) -> Unit,
    dismiss: () -> Unit
) {
    val size = 110.dp
    ModalBottomSheet(
        onDismissRequest = {
            dismiss()
        }, sheetState = sheetState
    ) {

        LazyVerticalGrid(
            columns = GridCells.Adaptive(size), modifier = Modifier.fillMaxWidth()
        ) {
            items(stickers.count(), key = { index -> stickers[index] }) { index ->
                val stickerId = stickers[index]
                Image(painter = painterResource(id = stickerId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(size)
                        .padding(4.dp)
                        .clickable {
                            selectedStickerId(stickerId)
                        })
            }
        }
    }
}

