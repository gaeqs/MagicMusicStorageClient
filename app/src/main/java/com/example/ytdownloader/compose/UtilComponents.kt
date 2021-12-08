package com.example.ytdownloader.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private val EXPAND_ANIMATION_DURATION = 200

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableContent(
    title: String,
    width: Float = 0.8f,
    scope: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .clickable { visible = !visible }
        .fillMaxWidth(width)) {
        Row {
            if (visible) {
                Icon(
                    imageVector = Icons.Default.ExpandLess,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "Collapse"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "Expand",
                )
            }

            Text(text = title, style = MaterialTheme.typography.h6)
        }

        AnimatedVisibility(visible = visible) {
            scope()
        }
    }
}