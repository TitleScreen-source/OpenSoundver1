package com.opensound.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigation(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
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
            .height(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0x229B5CFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            maxLines = 1
        )
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
