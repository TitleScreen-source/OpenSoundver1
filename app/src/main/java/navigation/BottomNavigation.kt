package com.opensound.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigation(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(Color(0xF2080710))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem(
            selected = currentScreen == "home",
            icon = "H",
            label = "Главная",
            onClick = { onScreenSelected("home") },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == "search",
            icon = "S",
            label = "Поиск",
            onClick = { onScreenSelected("search") },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == "library",
            icon = "M",
            label = "Библиотека",
            onClick = { onScreenSelected("library") },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == "profile" || currentScreen == "studio",
            icon = "P",
            label = "Профиль",
            onClick = { onScreenSelected("profile") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomItem(
    selected: Boolean,
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF9B5CFF)
    val inactiveColor = Color(0xFFA9A1B6)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0x229B5CFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
