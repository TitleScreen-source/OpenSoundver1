package com.opensound.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
    currentScreen: AudMoraScreen,
    onScreenSelected: (AudMoraScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
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
            selected = currentScreen == AudMoraScreen.Home,
            icon = "H",
            label = "\u0413\u043B\u0430\u0432\u043D\u0430\u044F",
            onClick = { onScreenSelected(AudMoraScreen.Home) },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == AudMoraScreen.Search,
            icon = "S",
            label = "\u041F\u043E\u0438\u0441\u043A",
            onClick = { onScreenSelected(AudMoraScreen.Search) },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == AudMoraScreen.Library,
            icon = "M",
            label = "\u0411\u0438\u0431\u043B\u0438\u043E\u0442\u0435\u043A\u0430",
            onClick = { onScreenSelected(AudMoraScreen.Library) },
            modifier = Modifier.weight(1f)
        )

        BottomItem(
            selected = currentScreen == AudMoraScreen.ArtistProfile ||
                currentScreen == AudMoraScreen.TrackStudio,
            icon = "P",
            label = "\u041F\u0440\u043E\u0444\u0438\u043B\u044C",
            onClick = { onScreenSelected(AudMoraScreen.ArtistProfile) },
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
