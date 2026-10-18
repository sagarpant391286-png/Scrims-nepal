package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Scrims", Icons.Default.SportsEsports, "nav_tab_home"),
    CHAT("Community", Icons.Default.Chat, "nav_tab_chat"),
    ADMIN("Admin Hub", Icons.Default.AdminPanelSettings, "nav_tab_admin"),
    SETTINGS("Wallet & Me", Icons.Default.Wallet, "nav_tab_settings")
}
