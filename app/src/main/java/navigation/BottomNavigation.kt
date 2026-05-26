package com.opensound.app.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigation(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFF111116)
    ) {
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = { onScreenSelected("home") },
            icon = { Text("") },
            label = { Text("Главная") }
        )

        NavigationBarItem(
            selected = currentScreen == "search",
            onClick = { onScreenSelected("search") },
            icon = { Text("") },
            label = { Text("Поиск") }
        )

        NavigationBarItem(
            selected = currentScreen == "library",
            onClick = { onScreenSelected("library") },
            icon = { Text("") },
            label = { Text("Библиотека") }
        )

        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = { onScreenSelected("profile") },
            icon = { Text("") },
            label = { Text("Профиль") }
        )
    }
}