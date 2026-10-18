package com.example.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.MatchResult
import com.example.model.Scrim
import com.example.model.ScrimStatus
import com.example.ui.components.ModeMapChip
import com.example.ui.components.PrizeTag
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrimDetailSheet(
    scrim: Scrim,
    isRegistered: Boolean,
    walletBalance: Int,
    onRegisterClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrandBackground,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = TextMuted)
        },
        modifier = Modifier.testTag("scrim_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header with image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandCardElevated)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.scrims_banner),
                    contentDescription = "Match Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, BrandBackground.copy(alpha = 0.9f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        ModeMapChip(mode = scrim.mode, map = scrim.map)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${scrim.mode.label} • ${scrim.map.displayName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    StatusBadge(status = scrim.status)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time & Slot Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandCardDark)
                    .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MATCH TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${scrim.matchDateFormatted}, ${scrim.matchTimeNepal}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SLOTS FILLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${scrim.slotsFilled} / ${scrim.slotsTotal} Teams",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (scrim.slotsFilled >= scrim.slotsTotal) WarningOrange else ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Room Credentials Box (Crucial for Scrims players!)
            val room = scrim.roomDetails
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isRegistered && room?.isReleased == true) CrimsonDark.copy(alpha = 0.35f) else BrandCardDark
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRegistered && room?.isReleased == true) CrimsonPrimary.copy(alpha = 0.6f) else BrandBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (room?.isReleased == true) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (room?.isReleased == true) GoldAccent else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CUSTOM ROOM CREDENTIALS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (room?.isReleased == true) GoldAccent else TextSecondary
                            )
                        }

                        if (room?.isReleased == true) {
                            Text(
                                text = "UNLOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        } else {
                            Text(
                                text = "LOCKS AT 15M PRIOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isRegistered) {
                        Text(
                            text = "Register for this scrim to access Room ID and Password once the admin publishes credentials.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else if (room?.isReleased == true) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandCardElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ROOM ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = room.roomId,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(BrandBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PASSWORD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = room.roomPassword,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Info", "ID: ${room.roomId} Pass: ${room.roomPassword}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room credentials copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("copy_room_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Room ID & Password")
                        }
                    } else {
                        Text(
                            text = "You are registered! The room ID and password will automatically unlock here 15 minutes before match start (${room?.releaseTimeNepal ?: "Prior to kickoff"}).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElectricBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prize Distribution
            Text(
                text = "PRIZE DISTRIBUTION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandCardDark)
                    .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                PrizeRow("1st Place Winner", "रू ${scrim.prizes.firstPlaceNpr}", GoldAccent)
                Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                PrizeRow("2nd Place Runner Up", "रू ${scrim.prizes.secondPlaceNpr}", TextPrimary)
                Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                PrizeRow("3rd Place", "रू ${scrim.prizes.thirdPlaceNpr}", TextSecondary)
                Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                PrizeRow("Top Fragger / MVP", "रू ${scrim.prizes.topFraggerNpr}", CrimsonPrimary)
                Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                PrizeRow("Per Kill Bounty", "रू ${scrim.prizes.perKillNpr}", ElectricBlue)
            }

            // Results if completed
            if (scrim.results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MATCH RESULTS & WINNERS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandCardDark)
                        .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    scrim.results.forEach { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${result.placement}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.placement == 1) GoldAccent else TextPrimary,
                                    modifier = Modifier.width(36.dp)
                                )
                                Column {
                                    Text(
                                        text = result.teamOrPlayerName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${result.kills} Kills • MVP: ${result.topFraggerIgn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            PrizeTag(amountNpr = result.prizeWonNpr)
                        }
                        if (result != scrim.results.last()) {
                            Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            if (isRegistered) {
                Button(
                    onClick = { /* Already registered */ },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("registered_status_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = SuccessGreen.copy(alpha = 0.2f),
                        disabledContentColor = SuccessGreen
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Slot Reserved & Confirmed", fontWeight = FontWeight.Bold)
                }
            } else if (scrim.slotsFilled >= scrim.slotsTotal) {
                Button(
                    onClick = { /* Full */ },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tournament Full (0 Slots Remaining)", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onRegisterClick(scrim.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_register_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Join Match (Entry: रू ${scrim.entryFeeNpr})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PrizeRow(title: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
