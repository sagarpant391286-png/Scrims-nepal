package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrimsRepository
import com.example.model.PaymentMethod
import com.example.model.TransactionType
import com.example.model.WalletTransaction
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: ScrimsRepository,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val transactions by repository.transactions.collectAsState()
    val withdrawals by repository.withdrawals.collectAsState()

    var showTopupDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WALLET & PROFILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Nepal payments, verified balance & gamer profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        },
        containerColor = BrandBackground,
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(CrimsonPrimary, GoldAccent))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.displayName.take(1),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = currentUser.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "FF IGN: ${currentUser.freeFireIgn} • UID: ${currentUser.freeFireUid}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GoldAccent
                                )
                                Text(
                                    text = currentUser.email,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandCardElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MATCHES", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${currentUser.totalMatchesPlayed}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BrandBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BOOYAH WINS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${currentUser.totalWins}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BrandBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WIN RATE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                val winRate = (currentUser.totalWins.toFloat() / currentUser.totalMatchesPlayed.toFloat() * 100).toInt()
                                Text("$winRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ElectricBlue)
                            }
                        }
                    }
                }
            }

            // Wallet Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandCardElevated
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AVAILABLE WALLET BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "रू ${"%,d".format(currentUser.walletBalanceNpr)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Instant Payouts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showTopupDialog = true },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("topup_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Top Up", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showWithdrawDialog = true },
                                modifier = Modifier.weight(1f).height(46.dp).testTag("withdraw_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Transactions Header
            item {
                SectionHeader(
                    title = "Recent Transactions",
                    subtitle = "History of tournament entries, wins and top-ups"
                )
            }

            items(transactions, key = { it.id }) { tx ->
                TransactionRow(tx = tx)
            }

            // Tournament Guidelines & FAQs
            item {
                SectionHeader(
                    title = "Rules & Fair Play Policy",
                    subtitle = "Official Scrims Nepal competitive standards"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        FaqItem(
                            question = "1. When are Custom Room details released?",
                            answer = "Room ID and Password automatically appear in the Scrim Details sheet and Official Chat precisely 15 minutes before match kickoff."
                        )
                        Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                        FaqItem(
                            question = "2. Are PC / Emulators allowed?",
                            answer = "Strictly mobile only. Any team found with emulator players will be immediately disqualified with no refund."
                        )
                        Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 8.dp))
                        FaqItem(
                            question = "3. How are withdrawal requests processed?",
                            answer = "Withdrawals to eSewa, Khalti, or Nepali bank accounts are reviewed and credited within 1-2 hours of match completion."
                        )
                    }
                }
            }

            // Sign out button
            item {
                OutlinedButton(
                    onClick = onSignOutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sign_out_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out of Scrims Nepal")
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showTopupDialog) {
        TopUpDialog(
            onDismiss = { showTopupDialog = false },
            onConfirmTopup = { amount, method ->
                repository.topUpWallet(amount, method)
                Toast.makeText(context, "रू $amount added to wallet via ${method.displayName}!", Toast.LENGTH_SHORT).show()
                showTopupDialog = false
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            maxAmount = currentUser.walletBalanceNpr,
            onDismiss = { showWithdrawDialog = false },
            onConfirmWithdraw = { amount, method, name, num ->
                val success = repository.requestWithdrawal(amount, method, name, num)
                if (success) {
                    Toast.makeText(context, "Withdrawal request of रू $amount submitted for approval!", Toast.LENGTH_LONG).show()
                    showWithdrawDialog = false
                } else {
                    Toast.makeText(context, "Insufficient balance!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun TransactionRow(tx: WalletTransaction) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (tx.amountNpr > 0) SuccessGreen.copy(alpha = 0.2f) else CrimsonPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.amountNpr > 0) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.amountNpr > 0) SuccessGreen else CrimsonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${tx.dateNepal} • ${tx.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = (if (tx.amountNpr > 0) "+रू " else "-रू ") + "%,d".format(kotlin.math.abs(tx.amountNpr)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (tx.amountNpr > 0) SuccessGreen else CrimsonPrimary
            )
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Column {
        Text(text = question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = answer, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onConfirmTopup: (Int, PaymentMethod) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.ESEWA) }
    var selectedAmount by remember { mutableStateOf(500) }

    val amounts = listOf(100, 250, 500, 1000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Top Up Nepal Esports Wallet", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Nepali Payment Gateway:", style = MaterialTheme.typography.labelMedium, color = GoldAccent)
                Spacer(modifier = Modifier.height(8.dp))

                PaymentMethod.entries.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method.displayName, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Amount (NPR):", style = MaterialTheme.typography.labelMedium, color = GoldAccent)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    amounts.forEach { amt ->
                        val isSel = selectedAmount == amt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CrimsonPrimary else BrandCardElevated)
                                .clickable { selectedAmount = amt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "रू $amt",
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmTopup(selectedAmount, selectedMethod) },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Confirm Top Up")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
}

@Composable
fun WithdrawDialog(
    maxAmount: Int,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (Int, PaymentMethod, String, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.ESEWA) }
    var amountText by remember { mutableStateOf("500") }
    var accountName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Request Prize Payout", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Available: रू $maxAmount", style = MaterialTheme.typography.bodySmall, color = GoldAccent)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (NPR)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Holder Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("eSewa ID / Khalti Mobile / Bank A/C") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 0
                    if (amt in 1..maxAmount && accountName.isNotBlank() && accountNumber.isNotBlank()) {
                        onConfirmWithdraw(amt, selectedMethod, accountName.trim(), accountNumber.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = BrandCardDark
    )
}
