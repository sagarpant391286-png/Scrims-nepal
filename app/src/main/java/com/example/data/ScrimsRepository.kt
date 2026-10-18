package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ScrimsRepository {

    private fun currentNepalTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun currentNepalDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Current User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_nepal_001",
            email = "sagarpant391286@gmail.com",
            displayName = "Sagar Pant (IG: Gorkhali)",
            freeFireIgn = "Gorkhali_FF",
            freeFireUid = "982314562",
            walletBalanceNpr = 1250,
            totalMatchesPlayed = 38,
            totalWins = 12
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Scrims List
    private val _scrims = MutableStateFlow<List<Scrim>>(
        listOf(
            Scrim(
                id = "scrim-101",
                map = ScrimMap.BERMUDA,
                mode = ScrimMode.SQUAD,
                status = ScrimStatus.LIVE,
                entryFeeNpr = 100,
                prizePoolNpr = 3000,
                matchTimeNepal = "06:30 PM",
                matchDateFormatted = "Today",
                slotsTotal = 12,
                slotsFilled = 12,
                isFeatured = true,
                roomDetails = RoomDetails(
                    roomId = "8492014",
                    roomPassword = "NEPAL",
                    instructions = "Strictly follow assigned slot. Mobile players only (No PC/Emulators allowed).",
                    isReleased = true,
                    releaseTimeNepal = "06:15 PM"
                ),
                prizes = PrizeBreakdown(
                    firstPlaceNpr = 1800,
                    secondPlaceNpr = 800,
                    thirdPlaceNpr = 400,
                    topFraggerNpr = 250,
                    perKillNpr = 25
                )
            ),
            Scrim(
                id = "scrim-102",
                map = ScrimMap.PURGATORY,
                mode = ScrimMode.DUO,
                status = ScrimStatus.UPCOMING,
                entryFeeNpr = 50,
                prizePoolNpr = 1400,
                matchTimeNepal = "08:00 PM",
                matchDateFormatted = "Today",
                slotsTotal = 24,
                slotsFilled = 18,
                isFeatured = false,
                roomDetails = RoomDetails(
                    roomId = "7391823",
                    roomPassword = "KTM",
                    instructions = "Room details will unlock 15 minutes before match kick-off.",
                    isReleased = false,
                    releaseTimeNepal = "07:45 PM"
                ),
                prizes = PrizeBreakdown(
                    firstPlaceNpr = 800,
                    secondPlaceNpr = 400,
                    thirdPlaceNpr = 200,
                    topFraggerNpr = 150,
                    perKillNpr = 15
                )
            ),
            Scrim(
                id = "scrim-103",
                map = ScrimMap.KALAHARI,
                mode = ScrimMode.SOLO,
                status = ScrimStatus.UPCOMING,
                entryFeeNpr = 30,
                prizePoolNpr = 900,
                matchTimeNepal = "09:15 PM",
                matchDateFormatted = "Today",
                slotsTotal = 48,
                slotsFilled = 32,
                isFeatured = false,
                prizes = PrizeBreakdown(
                    firstPlaceNpr = 500,
                    secondPlaceNpr = 250,
                    thirdPlaceNpr = 150,
                    topFraggerNpr = 100,
                    perKillNpr = 10
                )
            ),
            Scrim(
                id = "scrim-104",
                map = ScrimMap.ALPINE,
                mode = ScrimMode.SQUAD,
                status = ScrimStatus.COMPLETED,
                entryFeeNpr = 150,
                prizePoolNpr = 4500,
                matchTimeNepal = "04:00 PM",
                matchDateFormatted = "Today",
                slotsTotal = 12,
                slotsFilled = 12,
                isFeatured = false,
                results = listOf(
                    MatchResult(
                        placement = 1,
                        teamOrPlayerName = "Team Skylight Nepal",
                        kills = 16,
                        prizeWonNpr = 2600,
                        topFraggerIgn = "SL_Aayush",
                        isWinner = true,
                        placementPoints = 12
                    ),
                    MatchResult(
                        placement = 2,
                        teamOrPlayerName = "D13 Esports KTM",
                        kills = 9,
                        prizeWonNpr = 1200,
                        topFraggerIgn = "D13_Rohan",
                        isWinner = false,
                        placementPoints = 9
                    ),
                    MatchResult(
                        placement = 3,
                        teamOrPlayerName = "Gorkha Warriors",
                        kills = 7,
                        prizeWonNpr = 700,
                        topFraggerIgn = "GW_Niraj",
                        isWinner = false,
                        placementPoints = 8
                    )
                )
            )
        )
    )
    val scrims: StateFlow<List<Scrim>> = _scrims.asStateFlow()

    // Registered Scrim IDs for the current user
    private val _registeredScrimIds = MutableStateFlow<Set<String>>(setOf("scrim-101"))
    val registeredScrimIds: StateFlow<Set<String>> = _registeredScrimIds.asStateFlow()

    // Transactions History
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(
        listOf(
            WalletTransaction(
                id = "tx-1",
                title = "eSewa Topup Confirmed",
                amountNpr = 1000,
                dateNepal = "Today, 02:15 PM",
                status = "Completed",
                referenceId = "ESEWA-928173491",
                type = TransactionType.DEPOSIT_CONFIRMED
            ),
            WalletTransaction(
                id = "tx-2",
                title = "Entry Fee: Squad Bermuda Scrim",
                amountNpr = -100,
                dateNepal = "Today, 04:30 PM",
                status = "Paid",
                referenceId = "SCRIM-101",
                type = TransactionType.SCRIM_ENTRY
            ),
            WalletTransaction(
                id = "tx-3",
                title = "Prize Win: Duo Alpine Match #2",
                amountNpr = 350,
                dateNepal = "Yesterday, 08:45 PM",
                status = "Credited",
                referenceId = "WIN-9182",
                type = TransactionType.PRIZE_WIN
            )
        )
    )
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Withdrawals
    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(
        listOf(
            WithdrawalRequest(
                id = "wd-101",
                amountNpr = 500,
                method = PaymentMethod.ESEWA,
                accountName = "Sagar Pant",
                accountNumber = "9823145620",
                status = WithdrawalStatus.APPROVED,
                adminNote = "Paid via eSewa corporate batch #882."
            )
        )
    )
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    // Admin Users
    private val _adminUsers = MutableStateFlow<List<AdminUser>>(
        listOf(
            AdminUser(
                id = "admin-1",
                email = "sagarpant391286@gmail.com",
                role = AdminRole.SUPER_ADMIN,
                isSuperAdmin = true,
                permissions = AdminPermission.all(),
                invitedBy = "System Root",
                dateAddedNepal = "2026-09-01",
                lastActiveNepal = "Online now",
                status = AdminStatus.ACTIVE
            ),
            AdminUser(
                id = "admin-2",
                email = "mod.nepal@scrimsnepal.com",
                role = AdminRole.TOURNAMENT_MOD,
                isSuperAdmin = false,
                permissions = AdminPermission.moderatorDefaults(),
                invitedBy = "sagarpant391286@gmail.com",
                dateAddedNepal = "2026-09-15",
                lastActiveNepal = "15m ago",
                status = AdminStatus.ACTIVE
            ),
            AdminUser(
                id = "admin-3",
                email = "finance.pokhara@scrimsnepal.com",
                role = AdminRole.FINANCE_MOD,
                isSuperAdmin = false,
                permissions = AdminPermission.financeDefaults(),
                invitedBy = "sagarpant391286@gmail.com",
                dateAddedNepal = "2026-10-02",
                lastActiveNepal = "1h ago",
                status = AdminStatus.ACTIVE
            )
        )
    )
    val adminUsers: StateFlow<List<AdminUser>> = _adminUsers.asStateFlow()

    // Invitations
    private val _invitations = MutableStateFlow<List<AdminInvitation>>(
        listOf(
            AdminInvitation(
                id = "inv-01",
                email = "esports.referee@gmail.com",
                role = AdminRole.TOURNAMENT_MOD,
                permissions = AdminPermission.moderatorDefaults(),
                invitedBy = "sagarpant391286@gmail.com",
                status = InvitationStatus.PENDING,
                code = "NEPAL-MOD-774"
            )
        )
    )
    val invitations: StateFlow<List<AdminInvitation>> = _invitations.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(
        listOf(
            AdminAuditLog(
                id = "log-1",
                adminEmail = "sagarpant391286@gmail.com",
                action = AuditAction.ROOM_RELEASED,
                target = "Scrim Squad Bermuda (scrim-101)",
                timestampNepal = "06:15 PM",
                details = "Room ID: 8492014, Password released to 12 registered teams."
            ),
            AdminAuditLog(
                id = "log-2",
                adminEmail = "sagarpant391286@gmail.com",
                action = AuditAction.SCRIM_CREATED,
                target = "Scrim Duo Purgatory (scrim-102)",
                timestampNepal = "05:40 PM",
                details = "Entry Fee: NPR 50, Prize Pool: NPR 1,400"
            ),
            AdminAuditLog(
                id = "log-3",
                adminEmail = "sagarpant391286@gmail.com",
                action = AuditAction.WITHDRAWAL_APPROVED,
                target = "Withdrawal wd-101 (NPR 500)",
                timestampNepal = "03:10 PM",
                details = "Transferred via eSewa to 9823145620."
            )
        )
    )
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    // Conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(
        listOf(
            Conversation(
                id = "conv-official",
                title = "Official Scrims Nepal Announcements",
                type = ConversationType.ANNOUNCEMENTS,
                lastMessage = "Squad Bermuda Room ID & Password are now unlocked!",
                unreadCount = 1,
                isVerified = true,
                isOnline = true
            ),
            Conversation(
                id = "conv-support",
                title = "24/7 Kathmandu Support Helpdesk",
                type = ConversationType.OFFICIAL_SUPPORT,
                lastMessage = "Your deposit was verified successfully. Happy gaming!",
                unreadCount = 0,
                isVerified = true,
                isOnline = true
            ),
            Conversation(
                id = "conv-scrim-101",
                title = "Squad Bermuda (Scrim #101) Lobby",
                type = ConversationType.GROUP,
                lastMessage = "D13 Team captain present in Slot 4.",
                unreadCount = 3,
                isVerified = false,
                isOnline = true,
                groupMembersCount = 48
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Chat Messages Map (conversationId -> List<ChatMessage>)
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "conv-official" to listOf(
                ChatMessage(
                    id = "msg-10",
                    conversationId = "conv-official",
                    senderName = "Scrims Nepal Official",
                    senderUid = "official_bot",
                    message = "Welcome to Scrims Nepal Season 8! Daily competitive Free Fire rooms with verified instant payouts via eSewa & Khalti.",
                    timestampNepal = "05:00 PM",
                    isOfficialAdmin = true,
                    isMe = false
                ),
                ChatMessage(
                    id = "msg-11",
                    conversationId = "conv-official",
                    senderName = "Scrims Nepal Admin",
                    senderUid = "admin_super",
                    message = "Room details for Scrim #101 have been broadcasted. Please adhere strictly to room rules.",
                    timestampNepal = "06:15 PM",
                    isOfficialAdmin = true,
                    isMe = false,
                    attachedRoomData = "ID: 8492014 | PASS: NEPAL"
                )
            ),
            "conv-support" to listOf(
                ChatMessage(
                    id = "msg-20",
                    conversationId = "conv-support",
                    senderName = "Support Agent Bikash",
                    senderUid = "agent_bikash",
                    message = "Namaste Sagar! How can we assist your tournament experience today?",
                    timestampNepal = "02:00 PM",
                    isOfficialAdmin = true,
                    isMe = false
                ),
                ChatMessage(
                    id = "msg-21",
                    conversationId = "conv-support",
                    senderName = "Sagar",
                    senderUid = "user_nepal_001",
                    message = "Can I know when the room ID for Scrim #101 will be released?",
                    timestampNepal = "02:05 PM",
                    isOfficialAdmin = false,
                    isMe = true
                ),
                ChatMessage(
                    id = "msg-22",
                    conversationId = "conv-support",
                    senderName = "Support Agent Bikash",
                    senderUid = "agent_bikash",
                    message = "Room IDs are unlocked in the app precisely 15 minutes before match start time. You can also view it in the Scrim Details sheet.",
                    timestampNepal = "02:07 PM",
                    isOfficialAdmin = true,
                    isMe = false
                )
            ),
            "conv-scrim-101" to listOf(
                ChatMessage(
                    id = "msg-30",
                    conversationId = "conv-scrim-101",
                    senderName = "SL_Aayush",
                    senderUid = "u-sl",
                    message = "Skylight roster checked in. Ready for Bermuda!",
                    timestampNepal = "06:16 PM",
                    isOfficialAdmin = false,
                    isMe = false
                ),
                ChatMessage(
                    id = "msg-31",
                    conversationId = "conv-scrim-101",
                    senderName = "D13_Rohan",
                    senderUid = "u-d13",
                    message = "Slot 4 confirmed for D13.",
                    timestampNepal = "06:18 PM",
                    isOfficialAdmin = false,
                    isMe = false
                )
            )
        )
    )
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Actions
    fun registerForScrim(scrimId: String): Boolean {
        val scrim = _scrims.value.find { it.id == scrimId } ?: return false
        val user = _currentUser.value
        if (user.walletBalanceNpr < scrim.entryFeeNpr) {
            return false
        }
        if (scrim.slotsFilled >= scrim.slotsTotal) {
            return false
        }

        // Deduct wallet balance
        _currentUser.value = user.copy(
            walletBalanceNpr = user.walletBalanceNpr - scrim.entryFeeNpr,
            totalMatchesPlayed = user.totalMatchesPlayed + 1
        )

        // Increment slots
        _scrims.value = _scrims.value.map {
            if (it.id == scrimId) {
                val newFilled = it.slotsFilled + 1
                it.copy(
                    slotsFilled = newFilled,
                    status = if (newFilled >= it.slotsTotal) ScrimStatus.FULL else it.status
                )
            } else it
        }

        // Record registered ID
        _registeredScrimIds.value = _registeredScrimIds.value + scrimId

        // Add Transaction
        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(6)}",
            title = "Entry Fee: ${scrim.mode.label} ${scrim.map.displayName}",
            amountNpr = -scrim.entryFeeNpr,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Paid",
            referenceId = scrim.id,
            type = TransactionType.SCRIM_ENTRY
        )
        _transactions.value = listOf(tx) + _transactions.value

        return true
    }

    fun releaseRoomCredentials(scrimId: String, roomId: String, pass: String) {
        val timestamp = currentNepalTime()
        _scrims.value = _scrims.value.map {
            if (it.id == scrimId) {
                val currentRoom = it.roomDetails ?: RoomDetails()
                it.copy(
                    roomDetails = currentRoom.copy(
                        roomId = roomId,
                        roomPassword = pass,
                        isReleased = true,
                        releaseTimeNepal = timestamp
                    )
                )
            } else it
        }

        // Add audit log
        addAuditLog(
            action = AuditAction.ROOM_RELEASED,
            target = "Scrim $scrimId",
            details = "Room ID: $roomId, Password released."
        )

        // Broadcast to chat
        val announceMsg = ChatMessage(
            id = "msg-${UUID.randomUUID().toString().take(6)}",
            conversationId = "conv-official",
            senderName = "Scrims Nepal Admin",
            senderUid = "admin_super",
            message = "Room released for match $scrimId! ID: $roomId | Password: $pass",
            timestampNepal = timestamp,
            isOfficialAdmin = true,
            isMe = false,
            attachedRoomData = "ID: $roomId | PASS: $pass"
        )
        val currentConvMsgs = _messages.value["conv-official"].orEmpty()
        _messages.value = _messages.value + ("conv-official" to (currentConvMsgs + announceMsg))
    }

    fun publishResults(scrimId: String, resultsList: List<MatchResult>) {
        _scrims.value = _scrims.value.map {
            if (it.id == scrimId) {
                it.copy(
                    status = ScrimStatus.COMPLETED,
                    results = resultsList
                )
            } else it
        }

        addAuditLog(
            action = AuditAction.RESULTS_PUBLISHED,
            target = "Scrim $scrimId",
            details = "Published ${resultsList.size} team placements & distributed prize points."
        )
    }

    fun createScrim(scrim: Scrim) {
        _scrims.value = listOf(scrim) + _scrims.value
        addAuditLog(
            action = AuditAction.SCRIM_CREATED,
            target = "${scrim.mode.label} ${scrim.map.displayName}",
            details = "Entry: NPR ${scrim.entryFeeNpr}, Prize: NPR ${scrim.prizePoolNpr}"
        )
    }

    fun importBulkScrims(csvContent: String): CsvImportResult {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val validList = mutableListOf<CsvScrimRow>()
        val invalidList = mutableListOf<CsvScrimRow>()

        for ((index, line) in lines.withIndex()) {
            if (index == 0 && line.contains("mode", ignoreCase = true)) continue
            val parts = line.split(",").map { it.trim() }
            if (parts.size >= 4) {
                val mode = when (parts[0].uppercase()) {
                    "SOLO" -> ScrimMode.SOLO
                    "DUO" -> ScrimMode.DUO
                    "SQUAD" -> ScrimMode.SQUAD
                    else -> null
                }
                val map = when (parts[1].uppercase()) {
                    "BERMUDA" -> ScrimMap.BERMUDA
                    "PURGATORY" -> ScrimMap.PURGATORY
                    "KALAHARI" -> ScrimMap.KALAHARI
                    "ALPINE" -> ScrimMap.ALPINE
                    "NEXTERRA" -> ScrimMap.NEXTERRA
                    else -> null
                }
                val fee = parts[2].toIntOrNull()
                val time = parts[3]

                if (mode != null && map != null && fee != null && time.isNotBlank()) {
                    val row = CsvScrimRow(index + 1, mode, map, fee, time, true)
                    validList.add(row)
                    // Create scrim
                    val scrim = Scrim(
                        id = "scrim-csv-${UUID.randomUUID().toString().take(5)}",
                        map = map,
                        mode = mode,
                        entryFeeNpr = fee,
                        prizePoolNpr = fee * 20,
                        matchTimeNepal = time,
                        matchDateFormatted = "Today",
                        slotsTotal = if (mode == ScrimMode.SOLO) 48 else if (mode == ScrimMode.DUO) 24 else 12,
                        slotsFilled = 0
                    )
                    createScrim(scrim)
                } else {
                    invalidList.add(CsvScrimRow(index + 1, ScrimMode.SQUAD, ScrimMap.BERMUDA, 0, "", false, "Invalid columns format"))
                }
            } else {
                invalidList.add(CsvScrimRow(index + 1, ScrimMode.SQUAD, ScrimMap.BERMUDA, 0, "", false, "Expected at least 4 comma-separated values"))
            }
        }

        addAuditLog(
            action = AuditAction.BULK_SCRIMS_IMPORTED,
            target = "CSV Import",
            details = "Imported ${validList.size} scrims successfully. (${invalidList.size} errors)"
        )

        return CsvImportResult(lines.size, validList, invalidList)
    }

    fun requestWithdrawal(amount: Int, method: PaymentMethod, name: String, number: String): Boolean {
        val user = _currentUser.value
        if (amount <= 0 || amount > user.walletBalanceNpr) return false

        _currentUser.value = user.copy(walletBalanceNpr = user.walletBalanceNpr - amount)
        val wd = WithdrawalRequest(
            id = "wd-${UUID.randomUUID().toString().take(6)}",
            amountNpr = amount,
            method = method,
            accountName = name,
            accountNumber = number,
            status = WithdrawalStatus.PENDING
        )
        _withdrawals.value = listOf(wd) + _withdrawals.value

        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(6)}",
            title = "Withdrawal Request (${method.displayName})",
            amountNpr = -amount,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Pending Review",
            referenceId = wd.id,
            type = TransactionType.WITHDRAWAL_PAID
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    fun topUpWallet(amount: Int, method: PaymentMethod) {
        val user = _currentUser.value
        _currentUser.value = user.copy(walletBalanceNpr = user.walletBalanceNpr + amount)
        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(6)}",
            title = "${method.displayName} Topup Confirmed",
            amountNpr = amount,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Completed",
            referenceId = "TOPUP-${UUID.randomUUID().toString().take(8)}",
            type = TransactionType.DEPOSIT_CONFIRMED
        )
        _transactions.value = listOf(tx) + _transactions.value
    }

    fun approveWithdrawal(wdId: String, note: String) {
        _withdrawals.value = _withdrawals.value.map {
            if (it.id == wdId) it.copy(status = WithdrawalStatus.APPROVED, adminNote = note)
            else it
        }
        addAuditLog(
            action = AuditAction.WITHDRAWAL_APPROVED,
            target = "Withdrawal $wdId",
            details = "Approved and processed. Note: $note"
        )
    }

    fun inviteAdmin(email: String, role: AdminRole, perms: List<AdminPermission>): AdminInvitation {
        val inv = AdminInvitation(
            id = "inv-${UUID.randomUUID().toString().take(6)}",
            email = email,
            role = role,
            permissions = perms,
            invitedBy = _currentUser.value.email,
            status = InvitationStatus.PENDING,
            code = "NEPAL-${role.name.take(3)}-${(100..999).random()}"
        )
        _invitations.value = listOf(inv) + _invitations.value
        addAuditLog(
            action = AuditAction.ADMIN_INVITED,
            target = email,
            details = "Invited with role ${role.label} and ${perms.size} permissions."
        )
        return inv
    }

    fun updateAdminPermissions(adminId: String, role: AdminRole, perms: List<AdminPermission>) {
        _adminUsers.value = _adminUsers.value.map {
            if (it.id == adminId) it.copy(role = role, permissions = perms)
            else it
        }
        addAuditLog(
            action = AuditAction.ADMIN_ROLE_UPDATED,
            target = "Admin $adminId",
            details = "Updated to ${role.label} with ${perms.size} permissions."
        )
    }

    fun setAdminStatus(adminId: String, status: AdminStatus) {
        _adminUsers.value = _adminUsers.value.map {
            if (it.id == adminId) it.copy(status = status)
            else it
        }
        val action = when (status) {
            AdminStatus.ACTIVE -> AuditAction.ADMIN_REACTIVATED
            AdminStatus.SUSPENDED -> AuditAction.ADMIN_SUSPENDED
            AdminStatus.REVOKED -> AuditAction.ADMIN_REVOKED
        }
        addAuditLog(
            action = action,
            target = "Admin $adminId",
            details = "Status changed to ${status.label}."
        )
    }

    fun sendMessage(conversationId: String, messageText: String, replyTo: String? = null) {
        if (messageText.isBlank()) return
        val user = _currentUser.value
        val newMsg = ChatMessage(
            id = "msg-${UUID.randomUUID().toString().take(6)}",
            conversationId = conversationId,
            senderName = user.displayName,
            senderUid = user.id,
            message = messageText.trim(),
            timestampNepal = currentNepalTime(),
            isOfficialAdmin = false,
            isMe = true,
            replyToSender = replyTo
        )
        val currentList = _messages.value[conversationId].orEmpty()
        _messages.value = _messages.value + (conversationId to (currentList + newMsg))

        // Update conversation lastMessage
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = messageText.trim())
            else it
        }
    }

    private fun addAuditLog(action: AuditAction, target: String, details: String) {
        val log = AdminAuditLog(
            id = "log-${UUID.randomUUID().toString().take(6)}",
            adminEmail = _currentUser.value.email,
            action = action,
            target = target,
            timestampNepal = currentNepalTime(),
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
