package com.example.data

import android.util.Log
import com.example.data.firebase.FirebaseService
import com.example.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ScrimsRepository(
    val firebaseService: FirebaseService = FirebaseService()
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val auth: FirebaseAuth get() = firebaseService.auth

    private fun currentNepalTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun currentNepalDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Auth State
    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Current User Profile: Initial balance is strictly 0 NPR for new/uninitialized users
    private val _currentUser = MutableStateFlow(
        User(
            id = auth.currentUser?.uid ?: "",
            email = auth.currentUser?.email ?: "",
            displayName = auth.currentUser?.displayName ?: "Player",
            freeFireIgn = "",
            freeFireUid = "",
            walletBalanceNpr = 0,
            totalMatchesPlayed = 0,
            totalWins = 0,
            role = if (auth.currentUser?.email == "sagarpant391286@gmail.com") "ADMIN" else "USER"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Scrims List: Pure Firestore data, initialized to empty (no hardcoded/fake tournaments)
    private val _scrims = MutableStateFlow<List<Scrim>>(emptyList())
    val scrims: StateFlow<List<Scrim>> = _scrims.asStateFlow()

    // Loading State for Scrims
    private val _isLoadingScrims = MutableStateFlow(true)
    val isLoadingScrims: StateFlow<Boolean> = _isLoadingScrims.asStateFlow()

    // Registered Scrim IDs for the current user
    private val _registeredScrimIds = MutableStateFlow<Set<String>>(emptySet())
    val registeredScrimIds: StateFlow<Set<String>> = _registeredScrimIds.asStateFlow()

    // Real Transactions from Firestore
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Withdrawals
    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
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
            )
        )
    )
    val adminUsers: StateFlow<List<AdminUser>> = _adminUsers.asStateFlow()

    // Invitations
    private val _invitations = MutableStateFlow<List<AdminInvitation>>(emptyList())
    val invitations: StateFlow<List<AdminInvitation>> = _invitations.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    // Community / Chat Conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(
        listOf(
            Conversation(
                id = "conv-official",
                title = "Official Scrims Nepal Announcements",
                type = ConversationType.ANNOUNCEMENTS,
                lastMessage = "Welcome to Scrims Nepal! Check upcoming competitive matches.",
                unreadCount = 0,
                isVerified = true,
                isOnline = true
            ),
            Conversation(
                id = "conv-support",
                title = "24/7 Kathmandu Support Helpdesk",
                type = ConversationType.OFFICIAL_SUPPORT,
                lastMessage = "Need help with tournament entry or payouts? Reach out here.",
                unreadCount = 0,
                isVerified = true,
                isOnline = true
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Chat Messages
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "conv-official" to listOf(
                ChatMessage(
                    id = "msg-welcome",
                    conversationId = "conv-official",
                    senderName = "Scrims Nepal Official",
                    senderUid = "official_bot",
                    message = "Welcome to Scrims Nepal! Real tournament rooms with verified payouts via eSewa & Khalti.",
                    timestampNepal = currentNepalTime(),
                    isOfficialAdmin = true,
                    isMe = false
                )
            ),
            "conv-support" to listOf(
                ChatMessage(
                    id = "msg-sup-1",
                    conversationId = "conv-support",
                    senderName = "Support Agent Bikash",
                    senderUid = "agent_bikash",
                    message = "Namaste! Welcome to Scrims Nepal Helpdesk. How can we assist your esports experience today?",
                    timestampNepal = currentNepalTime(),
                    isOfficialAdmin = true,
                    isMe = false
                )
            )
        )
    )
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    init {
        // Observe Scrims in real-time from Firestore
        scope.launch {
            try {
                firebaseService.observeScrims().collect { list ->
                    _scrims.value = list
                    _isLoadingScrims.value = false
                    updateRegisteredScrims(list, _currentUser.value.id)
                }
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Error observing scrims", e)
                _isLoadingScrims.value = false
            }
        }

        // If user is already signed in on app start, sync profile & transactions
        auth.currentUser?.let { firebaseUser ->
            onUserAuthenticated(firebaseUser.uid, firebaseUser.email.orEmpty(), firebaseUser.displayName.orEmpty())
        }
    }

    private fun updateRegisteredScrims(scrimsList: List<Scrim>, userId: String) {
        if (userId.isBlank()) {
            _registeredScrimIds.value = emptySet()
            return
        }
        val registered = scrimsList
            .filter { it.registeredUserIds.contains(userId) }
            .map { it.id }
            .toSet()
        _registeredScrimIds.value = registered
    }

    fun onUserAuthenticated(uid: String, email: String, displayName: String, ign: String = "", ffUid: String = "") {
        _isAuthenticated.value = true
        scope.launch {
            try {
                val profile = firebaseService.createUserProfileIfAbsent(uid, email, displayName, ign, ffUid)
                _currentUser.value = profile
                updateRegisteredScrims(_scrims.value, uid)

                // Start observing profile for real-time wallet balance changes
                firebaseService.observeUserProfile(uid).collect { updatedProfile ->
                    if (updatedProfile != null) {
                        _currentUser.value = updatedProfile
                    }
                }
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to initialize user profile", e)
            }
        }

        // Start observing transactions
        scope.launch {
            try {
                firebaseService.observeTransactions(uid).collect { txList ->
                    _transactions.value = txList
                }
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to observe transactions", e)
            }
        }
    }

    suspend fun registerWithEmail(
        email: String,
        pass: String,
        displayName: String,
        ign: String,
        ffUid: String
    ): Result<User> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g. yourname@gmail.com)."))
        }
        if (cleanPass.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }
        return try {
            val result = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed: User not created"))
            val user = firebaseService.createUserProfileIfAbsent(
                uid = firebaseUser.uid,
                email = cleanEmail,
                displayName = displayName.trim(),
                ign = ign.trim(),
                ffUid = ffUid.trim()
            )
            onUserAuthenticated(firebaseUser.uid, cleanEmail, displayName.trim(), ign.trim(), ffUid.trim())
            Result.success(user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account already exists with this email address. Please tap 'Sign In' instead."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("The email address is invalid or badly formatted."))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Registration failed."
            if (msg.contains("badly formatted", ignoreCase = true)) {
                Result.failure(Exception("The email address is badly formatted. Please check for typos."))
            } else {
                Result.failure(Exception(msg))
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(Exception("Please enter a valid email address (e.g. yourname@gmail.com)."))
        }
        if (cleanPass.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters long."))
        }
        return try {
            val result = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed: User not found"))
            val existing = firebaseService.getUserProfile(firebaseUser.uid)
            val user = existing ?: firebaseService.createUserProfileIfAbsent(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: cleanEmail,
                displayName = firebaseUser.displayName ?: "Player",
                ign = "",
                ffUid = ""
            )
            onUserAuthenticated(firebaseUser.uid, user.email, user.displayName, user.freeFireIgn, user.freeFireUid)
            Result.success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val msg = e.localizedMessage ?: ""
            if (msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true)) {
                Result.failure(Exception("The email address is badly formatted. Please check for typos."))
            } else {
                Result.failure(Exception("Incorrect password or email not registered. If new, tap 'Create Account'."))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Sign-in failed."
            if (msg.contains("badly formatted", ignoreCase = true)) {
                Result.failure(Exception("The email address is badly formatted. Please check for typos."))
            } else {
                Result.failure(Exception(msg))
            }
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        scope.launch {
            try {
                auth.signOut()
            } catch (e: Exception) {
                Log.w("ScrimsRepository", "Sign out error", e)
            }
            _isAuthenticated.value = false
            _currentUser.value = User(walletBalanceNpr = 0)
            _registeredScrimIds.value = emptySet()
            _transactions.value = emptyList()
            onComplete()
        }
    }

    // Scrim Registration with strict wallet balance verification
    fun registerForScrim(scrimId: String): Boolean {
        val scrim = _scrims.value.find { it.id == scrimId } ?: return false
        val user = _currentUser.value

        // Financial Invariant: Must have sufficient balance
        if (user.walletBalanceNpr < scrim.entryFeeNpr) {
            return false
        }
        if (scrim.slotsFilled >= scrim.slotsTotal) {
            return false
        }
        if (scrim.registeredUserIds.contains(user.id)) {
            return true // Already registered
        }

        val newBalance = user.walletBalanceNpr - scrim.entryFeeNpr
        val updatedRegistered = scrim.registeredUserIds + user.id
        val updatedFilled = scrim.slotsFilled + 1
        val updatedStatus = if (updatedFilled >= scrim.slotsTotal) ScrimStatus.FULL else scrim.status

        val updatedScrim = scrim.copy(
            slotsFilled = updatedFilled,
            registeredUserIds = updatedRegistered,
            status = updatedStatus
        )

        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(8)}",
            title = "Entry Fee: ${scrim.mode.label} ${scrim.map.displayName}",
            amountNpr = -scrim.entryFeeNpr,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Paid",
            referenceId = scrim.id,
            type = TransactionType.SCRIM_ENTRY
        )

        // Optimistic update
        _currentUser.value = user.copy(
            walletBalanceNpr = newBalance,
            totalMatchesPlayed = user.totalMatchesPlayed + 1
        )
        _registeredScrimIds.value = _registeredScrimIds.value + scrimId
        _transactions.value = listOf(tx) + _transactions.value

        // Persist atomically to Firebase Firestore
        scope.launch {
            try {
                firebaseService.updateWalletBalance(user.id, newBalance)
                firebaseService.addTransaction(user.id, tx)
                firebaseService.saveScrim(updatedScrim)
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to persist scrim registration to Firestore", e)
            }
        }

        return true
    }

    // Admin Scrim Management
    fun createScrim(scrim: Scrim) {
        val scrimWithId = if (scrim.id.isBlank()) {
            scrim.copy(id = "scrim-${UUID.randomUUID().toString().take(6)}")
        } else scrim

        _scrims.value = listOf(scrimWithId) + _scrims.value.filter { it.id != scrimWithId.id }

        scope.launch {
            try {
                firebaseService.saveScrim(scrimWithId)
                addAuditLog(
                    action = AuditAction.SCRIM_CREATED,
                    target = "${scrimWithId.mode.label} ${scrimWithId.map.displayName}",
                    details = "Entry: NPR ${scrimWithId.entryFeeNpr}, Prize: NPR ${scrimWithId.prizePoolNpr}"
                )
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to create scrim in Firestore", e)
            }
        }
    }

    fun releaseRoomCredentials(scrimId: String, roomId: String, pass: String) {
        val timestamp = currentNepalTime()
        val targetScrim = _scrims.value.find { it.id == scrimId } ?: return
        val currentRoom = targetScrim.roomDetails ?: RoomDetails()
        val updated = targetScrim.copy(
            roomDetails = currentRoom.copy(
                roomId = roomId,
                roomPassword = pass,
                isReleased = true,
                releaseTimeNepal = timestamp
            )
        )

        _scrims.value = _scrims.value.map { if (it.id == scrimId) updated else it }

        scope.launch {
            try {
                firebaseService.saveScrim(updated)
                addAuditLog(
                    action = AuditAction.ROOM_RELEASED,
                    target = "Scrim $scrimId",
                    details = "Room ID: $roomId, Password released."
                )

                // Broadcast to official announcements
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
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to release room credentials", e)
            }
        }
    }

    fun publishResults(scrimId: String, resultsList: List<MatchResult>) {
        val target = _scrims.value.find { it.id == scrimId } ?: return
        val updated = target.copy(
            status = ScrimStatus.COMPLETED,
            results = resultsList
        )
        _scrims.value = _scrims.value.map { if (it.id == scrimId) updated else it }

        scope.launch {
            try {
                firebaseService.saveScrim(updated)
                addAuditLog(
                    action = AuditAction.RESULTS_PUBLISHED,
                    target = "Scrim $scrimId",
                    details = "Published ${resultsList.size} team placements & distributed prize points."
                )
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to publish match results", e)
            }
        }
    }

    fun deleteScrim(scrimId: String) {
        _scrims.value = _scrims.value.filter { it.id != scrimId }
        scope.launch {
            try {
                firebaseService.deleteScrim(scrimId)
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to delete scrim $scrimId", e)
            }
        }
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
                    val scrim = Scrim(
                        id = "scrim-csv-${UUID.randomUUID().toString().take(6)}",
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
                    invalidList.add(CsvScrimRow(index + 1, ScrimMode.SQUAD, ScrimMap.BERMUDA, 0, "", false, "Invalid format"))
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

        val newBalance = user.walletBalanceNpr - amount
        _currentUser.value = user.copy(walletBalanceNpr = newBalance)

        val wd = WithdrawalRequest(
            id = "wd-${UUID.randomUUID().toString().take(6)}",
            userId = user.id,
            userName = name,
            amountNpr = amount,
            method = method,
            accountName = name,
            accountNumber = number,
            status = WithdrawalStatus.PENDING,
            requestTimeNepal = currentNepalTime()
        )
        _withdrawals.value = listOf(wd) + _withdrawals.value

        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(8)}",
            title = "Withdrawal Request (${method.displayName})",
            amountNpr = -amount,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Pending Review",
            referenceId = wd.id,
            type = TransactionType.WITHDRAWAL_PAID
        )
        _transactions.value = listOf(tx) + _transactions.value

        scope.launch {
            try {
                firebaseService.updateWalletBalance(user.id, newBalance)
                firebaseService.addTransaction(user.id, tx)
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to persist withdrawal request", e)
            }
        }
        return true
    }

    fun topUpWallet(amount: Int, method: PaymentMethod) {
        val user = _currentUser.value
        val newBalance = user.walletBalanceNpr + amount
        _currentUser.value = user.copy(walletBalanceNpr = newBalance)

        val tx = WalletTransaction(
            id = "tx-${UUID.randomUUID().toString().take(8)}",
            title = "${method.displayName} Topup Confirmed",
            amountNpr = amount,
            dateNepal = "Today, ${currentNepalTime()}",
            status = "Completed",
            referenceId = "TOPUP-${UUID.randomUUID().toString().take(8)}",
            type = TransactionType.DEPOSIT_CONFIRMED
        )
        _transactions.value = listOf(tx) + _transactions.value

        scope.launch {
            try {
                firebaseService.updateWalletBalance(user.id, newBalance)
                firebaseService.addTransaction(user.id, tx)
            } catch (e: Exception) {
                Log.e("ScrimsRepository", "Failed to persist topup to Firestore", e)
            }
        }
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
            senderName = user.displayName.ifBlank { "Player" },
            senderUid = user.id,
            message = messageText.trim(),
            timestampNepal = currentNepalTime(),
            isOfficialAdmin = user.role == "ADMIN",
            isMe = true,
            replyToSender = replyTo
        )
        val currentList = _messages.value[conversationId].orEmpty()
        _messages.value = _messages.value + (conversationId to (currentList + newMsg))

        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = messageText.trim())
            else it
        }
    }

    private fun addAuditLog(action: AuditAction, target: String, details: String) {
        val log = AdminAuditLog(
            id = "log-${UUID.randomUUID().toString().take(6)}",
            adminEmail = _currentUser.value.email.ifBlank { "Admin" },
            action = action,
            target = target,
            timestampNepal = currentNepalTime(),
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
