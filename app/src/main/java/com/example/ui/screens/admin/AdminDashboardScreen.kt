package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrimsRepository
import com.example.model.RoomDetails
import com.example.model.Scrim
import com.example.ui.components.ModeMapChip
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    repository: ScrimsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrims by repository.scrims.collectAsState()

    var selectedAdminTab by remember { mutableStateOf("ROOMS") }
    var showCsvDialog by remember { mutableStateOf(false) }

    // Quick release room dialog
    var scrimForRoomRelease by remember { mutableStateOf<Scrim?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADMIN PORTAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { showCsvDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("open_csv_dialog_btn")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bulk CSV", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Admin Sub-tabs
                val tabs = listOf(
                    "ROOMS" to "Room Release",
                    "SCORING" to "Results & Scoring",
                    "STAFF" to "Staff RBAC",
                    "AUDIT" to "Audit Logs"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs) { (key, title) ->
                        val isSelected = selectedAdminTab == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CrimsonPrimary else BrandCardElevated)
                                .border(1.dp, if (isSelected) CrimsonPrimary else BrandBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedAdminTab = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("admin_tab_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        containerColor = BrandBackground,
        modifier = modifier.testTag("admin_dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedAdminTab) {
                "ROOMS" -> {
                    item {
                        SectionHeader(
                            title = "Match Room Release Center",
                            subtitle = "Publish Custom Room ID and Password 15 minutes before kickoff"
                        )
                    }

                    items(scrims, key = { it.id }) { scrim ->
                        RoomReleaseCard(
                            scrim = scrim,
                            onReleaseClick = { scrimForRoomRelease = scrim }
                        )
                    }
                }

                "SCORING" -> {
                    item {
                        ResultsScoringSection(repository = repository)
                    }
                }

                "STAFF" -> {
                    item {
                        AdminManagementSection(repository = repository)
                    }
                }

                "AUDIT" -> {
                    item {
                        AuditLogsSection(repository = repository)
                    }
                }
            }
        }
    }

    if (showCsvDialog) {
        BulkScrimCsvDialog(
            repository = repository,
            onDismiss = { showCsvDialog = false }
        )
    }

    scrimForRoomRelease?.let { scrim ->
        ReleaseRoomDialog(
            scrim = scrim,
            onDismiss = { scrimForRoomRelease = null },
            onConfirmRelease = { roomId, password ->
                repository.releaseRoomCredentials(scrim.id, roomId, password)
                Toast.makeText(context, "Room $roomId released and broadcasted to players!", Toast.LENGTH_LONG).show()
                scrimForRoomRelease = null
            }
        )
    }
}

@Composable
fun RoomReleaseCard(scrim: Scrim, onReleaseClick: () -> Unit) {
    val room = scrim.roomDetails
    val isReleased = room?.isReleased == true

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isReleased) SuccessGreen.copy(alpha = 0.5f) else BrandBorder
        ),
        modifier = Modifier.fillMaxWidth().testTag("room_card_${scrim.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeMapChip(mode = scrim.mode, map = scrim.map)
                Text(
                    text = "${scrim.matchDateFormatted} • ${scrim.matchTimeNepal}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isReleased && room != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.1f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RELEASED AT ${room.releaseTimeNepal}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Room ID: ${room.roomId} | Pass: ${room.roomPassword}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = onReleaseClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Update", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Credentials Locked",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningOrange
                        )
                        Text(
                            text = "${scrim.slotsFilled}/${scrim.slotsTotal} registered players waiting",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onReleaseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("release_room_btn_${scrim.id}")
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Release Room")
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseRoomDialog(
    scrim: Scrim,
    onDismiss: () -> Unit,
    onConfirmRelease: (String, String) -> Unit
) {
    var roomId by remember { mutableStateOf(scrim.roomDetails?.roomId ?: "") }
    var password by remember { mutableStateOf(scrim.roomDetails?.roomPassword ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Release Custom Room Details", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${scrim.mode.label} ${scrim.map.displayName} (${scrim.matchTimeNepal})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    label = { Text("Free Fire Custom Room ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_room_id")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Room Password") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_room_password")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomId.isNotBlank() && password.isNotBlank()) {
                        onConfirmRelease(roomId.trim(), password.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.testTag("submit_room_release_btn")
            ) {
                Text("Release & Notify Players")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
}
