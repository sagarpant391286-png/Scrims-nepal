package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ScrimsRepository
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.chat.ChatHomeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.*

@Composable
fun MainScreen(
    repository: ScrimsRepository,
    modifier: Modifier = Modifier
) {
    var isSignedIn by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(BottomNavTab.HOME) }
    var activeChatConversationId by remember { mutableStateOf<String?>(null) }

    if (!isSignedIn) {
        AuthScreen(
            onSignInSuccess = { isSignedIn = true },
            modifier = modifier
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BrandCardDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab && activeChatConversationId == null
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            activeChatConversationId = null
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CrimsonPrimary,
                            selectedTextColor = CrimsonPrimary,
                            indicatorColor = CrimsonPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = BrandBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                activeChatConversationId != null -> {
                    ChatDetailScreen(
                        conversationId = activeChatConversationId!!,
                        repository = repository,
                        onBackClick = { activeChatConversationId = null }
                    )
                }
                currentTab == BottomNavTab.HOME -> {
                    HomeScreen(
                        repository = repository,
                        onNavigateToWallet = { currentTab = BottomNavTab.SETTINGS }
                    )
                }
                currentTab == BottomNavTab.CHAT -> {
                    ChatHomeScreen(
                        repository = repository,
                        onOpenConversation = { convId ->
                            activeChatConversationId = convId
                        }
                    )
                }
                currentTab == BottomNavTab.ADMIN -> {
                    AdminDashboardScreen(
                        repository = repository
                    )
                }
                currentTab == BottomNavTab.SETTINGS -> {
                    SettingsScreen(
                        repository = repository,
                        onSignOutClick = { isSignedIn = false }
                    )
                }
            }
        }
    }
}
