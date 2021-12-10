package com.example.ytdownloader.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector) {

    companion object {
        val ITEMS = listOf(Main, Sections)
    }

    object Main : Screen("main", Icons.Default.Home)
    object Sections : Screen("sections", Icons.Default.MusicNote)

}