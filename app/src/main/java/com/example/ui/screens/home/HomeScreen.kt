package com.example.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ScrimsRepository
import com.example.model.Scrim
import com.example.model.ScrimMode
import com.example.model.ScrimStatus
import com.example.ui.components.ModeMapChip
import com.example.ui.components.PrizeTag
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: ScrimsRepository,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrims by repository.scrims.collectAsState()
    val registeredIds by repository.registeredScrimIds.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var activeScrimForDetails by remember { mutableStateOf<Scrim?>(null) }

    val filteredScrims = remember(scrims, selectedFilter) {
        when (selectedFilter) {
            "SOLO" -> scrims.filter { it.mode == ScrimMode.SOLO }
            "DUO" -> scrims.filter { it.mode == ScrimMode.DUO }
            "SQUAD" -> scrims.filter { it.mode == ScrimMode.SQUAD }
            "LIVE" -> scrims.filter { it.status == ScrimStatus.LIVE }
            "UPCOMING" -> scrims.filter { it.status == ScrimStatus.UPCOMING }
            else -> scrims
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                walletBalance = currentUser.walletBalanceNpr,
                onWalletClick = onNavigateToWallet
            )
        },
        containerColor = BrandBackground,
        modifier = modifier.testTag("home_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Featured Scrim Banner
            item {
                val featured = scrims.find { it.isFeatured } ?: scrims.firstOrNull()
                if (featured != null) {
                    FeaturedScrimBanner(
                        scrim = featured,
                        isRegistered = registeredIds.contains(featured.id),
                        onClick = { activeScrimForDetails = featured }
                    )
                }
            }

            // Filter Tabs
            item {
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it }
                )
            }

            // Scrims List Header
            item {
                SectionHeader(
                    title = "Tournaments & Scrims",
                    subtitle = "${filteredScrims.size} matches available in Nepal timezone"
                )
            }

            // List of Scrims
            items(filteredScrims, key = { it.id }) { scrim ->
                ScrimCard(
                    scrim = scrim,
                    isRegistered = registeredIds.contains(scrim.id),
                    onClick = { activeScrimForDetails = scrim }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Bottom Sheet for Scrim Details
    activeScrimForDetails?.let { scrim ->
        ScrimDetailSheet(
            scrim = scrim,
            isRegistered = registeredIds.contains(scrim.id),
            walletBalance = currentUser.walletBalanceNpr,
            onRegisterClick = { scrimId ->
                val success = repository.registerForScrim(scrimId)
                if (success) {
                    Toast.makeText(context, "Registration Confirmed! Slot reserved.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Insufficient wallet balance! Please top up.", Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { activeScrimForDetails = null }
        )
    }
}

@Composable
fun HomeTopBar(walletBalance: Int, onWalletClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandCardElevated)
                    .border(1.5.dp, CrimsonPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.scrims_logo),
                    contentDescription = "Scrims Nepal Logo",
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCRIMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEPAL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = CrimsonPrimary
                    )
                }
                Text(
                    text = "Esports & Daily Custom Rooms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        // Wallet Balance Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BrandCardElevated)
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable { onWalletClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("top_wallet_chip"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Wallet",
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "रू $walletBalance",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun FeaturedScrimBanner(scrim: Scrim, isRegistered: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() }
            .testTag("featured_banner_card"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.8f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.scrims_banner),
                contentDescription = "Featured Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, BrandBackground.copy(alpha = 0.95f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FEATURED SHOWDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = BrandBackground
                        )
                    }

                    StatusBadge(status = scrim.status)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "${scrim.mode.label} ${scrim.map.displayName} Championship",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kickoff: ${scrim.matchTimeNepal} Nepal Standard Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL PRIZE",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent
                        )
                        PrizeTag(amountNpr = scrim.prizePoolNpr)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(selectedFilter: String, onSelectFilter: (String) -> Unit) {
    val filters = listOf(
        "ALL" to "All Scrims",
        "SQUAD" to "Squad",
        "DUO" to "Duo",
        "SOLO" to "Solo",
        "LIVE" to "🔴 Live Now",
        "UPCOMING" to "Upcoming"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CrimsonPrimary else BrandCardElevated)
                    .border(
                        1.dp,
                        if (isSelected) CrimsonPrimary else BrandBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectFilter(key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("filter_chip_$key"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun ScrimCard(scrim: Scrim, isRegistered: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("scrim_card_${scrim.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRegistered) SuccessGreen.copy(alpha = 0.5f) else BrandBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeMapChip(mode = scrim.mode, map = scrim.map)
                StatusBadge(status = scrim.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timing & Prize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${scrim.matchDateFormatted} at ${scrim.matchTimeNepal}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Entry Fee: रू ${scrim.entryFeeNpr} per player",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                PrizeTag(amountNpr = scrim.prizePoolNpr)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slots progress
            val slotRatio = scrim.slotsFilled.toFloat() / scrim.slotsTotal.toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SLOTS FILLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${scrim.slotsFilled}/${scrim.slotsTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (scrim.slotsFilled >= scrim.slotsTotal) WarningOrange else ElectricBlue,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { slotRatio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (scrim.slotsFilled >= scrim.slotsTotal) WarningOrange else CrimsonPrimary,
                    trackColor = BrandCardElevated
                )
            }

            if (isRegistered) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "You are registered! Tap to view Room ID & Password",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
