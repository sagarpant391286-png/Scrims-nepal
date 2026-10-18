package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Publish
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
import com.example.model.MatchResult
import com.example.model.Scrim
import com.example.ui.components.PrizeTag
import com.example.ui.theme.*

data class EditablePlacement(
    val placement: Int,
    var teamName: String,
    var killsText: String,
    var topFragger: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScoringSection(
    repository: ScrimsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrims by repository.scrims.collectAsState()

    var selectedScrim by remember { mutableStateOf<Scrim?>(scrims.firstOrNull()) }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Placements state
    val placements = remember {
        mutableStateListOf(
            EditablePlacement(1, "Team Skylight Nepal", "14", "SL_Aayush"),
            EditablePlacement(2, "D13 Esports KTM", "10", "D13_Rohan"),
            EditablePlacement(3, "Gorkha Warriors", "8", "GW_Niraj"),
            EditablePlacement(4, "High Altitude", "5", "HA_Sonam")
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("results_scoring_section")
    ) {
        Text(
            text = "Match Results & Prize Distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Enter placements and kill points to distribute prizes directly to players' wallets",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scrim Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown }
        ) {
            OutlinedTextField(
                value = selectedScrim?.let { "${it.mode.label} ${it.map.displayName} (${it.matchTimeNepal})" } ?: "Select Scrim",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Match Scrim") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = BrandBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BrandCardDark,
                    unfocusedContainerColor = BrandCardDark
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("scrim_select_dropdown")
            )

            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.background(BrandCardDark)
            ) {
                scrims.forEach { scrim ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${scrim.mode.label} ${scrim.map.displayName} • ${scrim.matchTimeNepal} (Pool: रू ${scrim.prizePoolNpr})",
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            selectedScrim = scrim
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Placements list
        Text(
            text = "LEADERBOARD PLACEMENTS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            placements.forEachIndexed { index, item ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (item.placement == 1) GoldAccent else CrimsonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${item.placement}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = BrandBackground
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = item.teamName,
                            onValueChange = { placements[index] = item.copy(teamName = it) },
                            label = { Text("Team / Player", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = item.killsText,
                            onValueChange = { placements[index] = item.copy(killsText = it) },
                            label = { Text("Kills", fontSize = 10.sp) },
                            modifier = Modifier.width(64.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedTextField(
                            value = item.topFragger,
                            onValueChange = { placements[index] = item.copy(topFragger = it) },
                            label = { Text("MVP IGN", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val current = selectedScrim ?: return@Button
                val resultsList = placements.map { p ->
                    val kills = p.killsText.toIntOrNull() ?: 0
                    val prize = when (p.placement) {
                        1 -> current.prizes.firstPlaceNpr + (kills * current.prizes.perKillNpr)
                        2 -> current.prizes.secondPlaceNpr + (kills * current.prizes.perKillNpr)
                        3 -> current.prizes.thirdPlaceNpr + (kills * current.prizes.perKillNpr)
                        else -> kills * current.prizes.perKillNpr
                    }
                    MatchResult(
                        placement = p.placement,
                        teamOrPlayerName = p.teamName,
                        kills = kills,
                        prizeWonNpr = prize,
                        topFraggerIgn = p.topFragger,
                        isWinner = p.placement == 1,
                        placementPoints = if (p.placement == 1) 12 else if (p.placement == 2) 9 else 6
                    )
                }

                repository.publishResults(current.id, resultsList)
                Toast.makeText(context, "Results successfully published & prizes credited!", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("publish_results_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BrandBackground)
        ) {
            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish Official Results & Credit Wallets", fontWeight = FontWeight.Bold)
        }
    }
}
