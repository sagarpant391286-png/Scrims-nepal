package com.example.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrimsRepository
import com.example.model.*
import com.example.ui.components.ModeMapChip
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    repository: ScrimsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrims by repository.scrims.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val isAdmin = currentUser.role == "ADMIN" || currentUser.email == "sagarpant391286@gmail.com"

    var selectedAdminTab by remember { mutableStateOf("ROOMS") }
    var showCsvDialog by remember { mutableStateOf(false) }
    var showCreateScrimDialog by remember { mutableStateOf(false) }

    // Quick release room dialog
    var scrimForRoomRelease by remember { mutableStateOf<Scrim?>(null) }
    var scrimToDelete by remember { mutableStateOf<Scrim?>(null) }

    if (!isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BrandBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CrimsonPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Admin Hub Access Restricted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Logged in as ${currentUser.email}.\nTournament creation and custom room releases are restricted to authorized Scrims Nepal moderators and admins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showCreateScrimDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            modifier = Modifier.testTag("open_create_scrim_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Match", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCsvDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_csv_dialog_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", style = MaterialTheme.typography.labelSmall)
                        }
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

                    if (scrims.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Tournaments in Firestore",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap '+ Match' above or 'CSV' to schedule real matches.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(scrims, key = { it.id }) { scrim ->
                            RoomReleaseCard(
                                scrim = scrim,
                                onReleaseClick = { scrimForRoomRelease = scrim },
                                onDeleteClick = { scrimToDelete = scrim }
                            )
                        }
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

    if (showCreateScrimDialog) {
        CreateScrimDialog(
            onDismiss = { showCreateScrimDialog = false },
            onCreateScrim = { newScrim ->
                repository.createScrim(newScrim)
                Toast.makeText(context, "Tournament created and saved to Firestore!", Toast.LENGTH_SHORT).show()
                showCreateScrimDialog = false
            }
        )
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

    scrimToDelete?.let { scrim ->
        AlertDialog(
            onDismissRequest = { scrimToDelete = null },
            title = { Text("Delete Tournament", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text("Are you sure you want to delete ${scrim.mode.label} ${scrim.map.displayName} ($scrim.id) from Firestore?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteScrim(scrim.id)
                        Toast.makeText(context, "Tournament deleted from Firestore.", Toast.LENGTH_SHORT).show()
                        scrimToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { scrimToDelete = null }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = BrandCardDark
        )
    }
}

@Composable
fun RoomReleaseCard(
    scrim: Scrim,
    onReleaseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${scrim.matchDateFormatted} • ${scrim.matchTimeNepal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
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
                            text = "${scrim.slotsFilled}/${scrim.slotsTotal} registered players",
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
fun CreateScrimDialog(
    onDismiss: () -> Unit,
    onCreateScrim: (Scrim) -> Unit
) {
    var selectedMap by remember { mutableStateOf(ScrimMap.BERMUDA) }
    var selectedMode by remember { mutableStateOf(ScrimMode.SQUAD) }
    var feeText by remember { mutableStateOf("50") }
    var prizeText by remember { mutableStateOf("1500") }
    var timeText by remember { mutableStateOf("07:00 PM") }
    var slotsText by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Real Tournament (Firestore)", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Map & Mode:", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScrimMode.entries.forEach { mode ->
                        val isSel = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CrimsonPrimary else BrandCardElevated)
                                .clickable {
                                    selectedMode = mode
                                    slotsText = if (mode == ScrimMode.SOLO) "48" else if (mode == ScrimMode.DUO) "24" else "12"
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mode.label, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ScrimMap.entries.forEach { map ->
                        val isSel = selectedMap == map
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CrimsonPrimary else BrandCardElevated)
                                .clickable { selectedMap = map }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(map.displayName.take(5), fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Fee (NPR)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prizeText,
                        onValueChange = { prizeText = it },
                        label = { Text("Prize (NPR)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Match Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slotsText,
                        onValueChange = { slotsText = it },
                        label = { Text("Slots") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeText.toIntOrNull() ?: 50
                    val prize = prizeText.toIntOrNull() ?: 1500
                    val slots = slotsText.toIntOrNull() ?: 12
                    val newScrim = Scrim(
                        id = "scrim-${UUID.randomUUID().toString().take(6)}",
                        map = selectedMap,
                        mode = selectedMode,
                        entryFeeNpr = fee,
                        prizePoolNpr = prize,
                        matchTimeNepal = timeText.trim(),
                        matchDateFormatted = "Today",
                        slotsTotal = slots,
                        slotsFilled = 0,
                        isFeatured = false
                    )
                    onCreateScrim(newScrim)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text("Publish to Firestore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
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
