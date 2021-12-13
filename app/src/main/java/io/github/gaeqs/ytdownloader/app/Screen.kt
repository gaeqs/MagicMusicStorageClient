package io.github.gaeqs.ytdownloader.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector) {

    companion object {
        val ITEMS = listOf(Main, Sections, Albums, Status)
    }

    object Main : Screen("main", Icons.Default.Home)
    object Sections : Screen("sections", Icons.Default.MusicNote)
    object Albums : Screen("albums", Icons.Default.Image)
    object Status : Screen("status", Icons.Default.Download)

}