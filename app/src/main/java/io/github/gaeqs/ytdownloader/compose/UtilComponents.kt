package io.github.gaeqs.ytdownloader.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import io.github.gaeqs.ytdownloader.R
import io.github.gaeqs.ytdownloader.app.Screen

private val EXPAND_ANIMATION_DURATION = 200

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppIcon(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .clickable { expanded = !expanded }
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(R.drawable.yt_downloader_crack), null)
        AnimatedVisibility(expanded) {
            Text(
                text = "Magic Music Storage",
                style = MaterialTheme.typography.h2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableContent(
    title: String,
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    width: Float = 0.8f,
    rowScope: @Composable RowScope.() -> Unit = {
        Text(text = title, style = MaterialTheme.typography.h6)
    },
    scope: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(width)) {
        Row(
            rowModifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { visible = !visible },
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            rowScope()
        }

        AnimatedVisibility(visible = visible) {
            scope()
        }
    }
}

@Composable
fun MainNav(nav: NavController) {
    BottomAppBar {
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Screen.ITEMS.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    nav.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun DropDownMenu(
    expanded: Boolean,
    title: String,
    elements: List<String>,
    modifier: Modifier = Modifier,
    selectedElement: String = elements.firstOrNull() ?: "",
    onExpand: (Boolean) -> Unit = {},
    onSelectElement: (String) -> Unit = {},
    itemBuilder: @Composable RowScope.(String) -> Unit = { Text(text = it) },
    extraItems: @Composable ColumnScope.() -> Unit = {}
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column(modifier) {
        OutlinedTextField(
            value = selectedElement,
            onValueChange = onSelectElement,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text(title) },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable { onExpand(!expanded) })
            },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpand(false) },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            elements.forEach { label ->
                DropdownMenuItem(
                    onClick = {
                    onSelectElement(label)
                    onExpand(false)
                }) {
                    itemBuilder(label)
                }
            }
            extraItems()
        }
    }
}
