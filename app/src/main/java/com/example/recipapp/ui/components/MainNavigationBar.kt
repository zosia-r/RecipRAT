package com.example.recipapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.recipapp.ui.screens.Screen


// Composable for the bottom navigation bar
@Composable
fun MainNavigationBar(
    currentDestination: NavDestination?,
    onTabClick: (String) -> Unit,
    onNewClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val favSelected = currentDestination?.hierarchy
            ?.any { it.route == Screen.Favourites.route } == true

        NavigationBarItem(
            icon = {
                Icon(
                    Screen.Favourites.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            label    = { Text(Screen.Favourites.label, style = MaterialTheme.typography.labelSmall) },
            selected = favSelected,
            onClick  = { onTabClick(Screen.Favourites.route) },
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor   = MaterialTheme.colorScheme.tertiary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor   = MaterialTheme.colorScheme.tertiary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.tertiaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Screen.New.icon,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            label    = {
                Text(
                    "New",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selected = false,
            onClick  = onNewClick,
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor   = Color.Transparent,
                unselectedIconColor = Color.Transparent,
                indicatorColor      = Color.Transparent
            )
        )

        val searchSelected = currentDestination?.hierarchy
            ?.any { it.route == Screen.Search.route } == true

        NavigationBarItem(
            icon = {
                Icon(
                    Screen.Search.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            label    = { Text(Screen.Search.label, style = MaterialTheme.typography.labelSmall) },
            selected = searchSelected,
            onClick  = { onTabClick(Screen.Search.route) },
            colors   = NavigationBarItemDefaults.colors(
                selectedIconColor   = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor   = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor      = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}