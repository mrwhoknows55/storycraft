package com.mrwhoknows.storycraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.mrwhoknows.storycraft.ui.screen.EditorScreen
import com.mrwhoknows.storycraft.ui.screen.EditorViewModel
import com.mrwhoknows.storycraft.ui.theme.StoryCraftTheme
import com.mrwhoknows.storycraft.util.shareOnIGStory
import androidx.compose.runtime.getValue
import com.mrwhoknows.storycraft.util.getImageBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoryCraftTheme {
                val viewModel by viewModels<EditorViewModel>()
                val state by viewModel.state.collectAsState()
                EditorScreen(
                    photoState = state,
                    onAction = viewModel::onAction,
                    onStoryShareClick = {
                        shareOnIGStory(getImageBitmap(it))
                    }
                )
            }
        }
    }
}