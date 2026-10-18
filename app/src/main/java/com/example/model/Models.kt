package com.example.model

enum class ScrimMap(val displayName: String) {
    BERMUDA("Bermuda"),
    PURGATORY("Purgatory"),
    KALAHARI("Kalahari"),
    ALPINE("Alpine"),
    NEXTERRA("NexTerra")
}

enum class ScrimMode(val label: String, val teamSize: Int) {
    SOLO("Solo", 1),
    DUO("Duo", 2),
    SQUAD("Squad", 4)
}

enum class ScrimStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE("Live Now"),
    FULL("Full"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    DRAFT("Draft")
}

data class RoomDetails(
    val roomId: String = "",
    val roomPassword: String = "",
    val instructions: String = "Join slot according to team list. Teaming or hacking will result in an immediate ban.",
    val isReleased: Boolean = false,
    val releaseTimeNepal: String = ""
)

data class PrizeBreakdown(
    val firstPlaceNpr: Int = 1000,
    val secondPlaceNpr: Int = 500,
    val thirdPlaceNpr: Int = 250,
    val topFraggerNpr: Int = 200,
    val perKillNpr: Int = 20
)

data class MatchResult(
    val placement: Int = 1,
    val teamOrPlayerName: String = "",
    val kills: Int = 0,
    val prizeWonNpr: Int = 0,
    val topFraggerIgn: String = "",
    val isWinner: Boolean = false,
    val placementPoints: Int = 0
)

data class TeamMember(
    val uid: String = "",
    val ign: String = ""
)

data class Registration(
    val id: String = "",
    val captainUid: String = "",
    val captainIgn: String = "",
    val captainPhone: String = "",
    val members: List<TeamMember> = emptyList(),
    val isConfirmed: Boolean = true
)

data class Scrim(
    val id: String,
    val map: ScrimMap,
    val mode: ScrimMode,
    val status: ScrimStatus = ScrimStatus.UPCOMING,
    val entryFeeNpr: Int = 50,
    val prizePoolNpr: Int = 1500,
    val matchTimeNepal: String = "07:00 PM",
    val matchDateFormatted: String = "Today",
    val slotsTotal: Int = 12,
    val slotsFilled: Int = 8,
    val isFeatured: Boolean = false,
    val roomDetails: RoomDetails? = null,
    val prizes: PrizeBreakdown = PrizeBreakdown(),
    val results: List<MatchResult> = emptyList()
)

enum class PaymentMethod(val displayName: String) {
    ESEWA("eSewa Nepal"),
    KHALTI("Khalti Digital Wallet"),
    IME_PAY("IME Pay"),
    BANK_TRANSFER("Nepal Bank Transfer")
}

enum class TransactionType {
    DEPOSIT_CONFIRMED,
    SCRIM_ENTRY,
    PRIZE_WIN,
    WITHDRAWAL_PAID,
    WITHDRAWAL_REVERSED
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val amountNpr: Int,
    val dateNepal: String,
    val status: String,
    val referenceId: String,
    val type: TransactionType
)

enum class WithdrawalStatus(val label: String) {
    PENDING("Pending Approval"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    PAID("Paid / Completed"),
    REJECTED("Rejected")
}

data class WithdrawalRequest(
    val id: String,
    val amountNpr: Int,
    val method: PaymentMethod,
    val accountName: String,
    val accountNumber: String,
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val adminNote: String = ""
)

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String = "",
    val freeFireIgn: String = "Gorkhali_FF",
    val freeFireUid: String = "194829381",
    val walletBalanceNpr: Int = 850,
    val totalMatchesPlayed: Int = 42,
    val totalWins: Int = 14
)

enum class AdminRole(val label: String) {
    SUPER_ADMIN("Super Admin"),
    TOURNAMENT_MOD("Tournament Moderator"),
    FINANCE_MOD("Finance Moderator"),
    SUPPORT_AGENT("Support Agent")
}

enum class AdminStatus(val label: String) {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    REVOKED("Revoked")
}

enum class AdminPermission(val label: String) {
    MANAGE_SCRIMS("Manage & Schedule Scrims"),
    RELEASE_ROOM("Release Room ID & Password"),
    ENTER_RESULTS("Score Matches & Distribute Prizes"),
    MANAGE_WALLET("Approve Withdrawals & Top-ups"),
    MANAGE_ADMINS("Manage Staff & Permissions"),
    VIEW_AUDIT_LOGS("View System Audit Logs");

    companion object {
        fun all(): List<AdminPermission> = entries
        fun moderatorDefaults(): List<AdminPermission> = listOf(MANAGE_SCRIMS, RELEASE_ROOM, ENTER_RESULTS)
        fun financeDefaults(): List<AdminPermission> = listOf(MANAGE_WALLET, VIEW_AUDIT_LOGS)
        fun supportDefaults(): List<AdminPermission> = listOf(RELEASE_ROOM)
    }
}

data class AdminUser(
    val id: String,
    val email: String,
    val role: AdminRole,
    val isSuperAdmin: Boolean = false,
    val permissions: List<AdminPermission> = emptyList(),
    val invitedBy: String = "Root Admin",
    val dateAddedNepal: String = "2026-10-01",
    val lastActiveNepal: String = "Just now",
    val status: AdminStatus = AdminStatus.ACTIVE
)

enum class InvitationStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
}

data class AdminInvitation(
    val id: String,
    val email: String,
    val role: AdminRole,
    val permissions: List<AdminPermission>,
    val invitedBy: String,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val code: String = ""
)

enum class AuditAction(val label: String) {
    SCRIM_CREATED("Scrim Match Created"),
    ROOM_RELEASED("Room Credentials Released"),
    RESULTS_PUBLISHED("Match Results Published"),
    BULK_SCRIMS_IMPORTED("Bulk Scrims CSV Imported"),
    WITHDRAWAL_APPROVED("Withdrawal Approved"),
    ADMIN_INVITED("New Admin Invited"),
    ADMIN_ROLE_UPDATED("Admin Permissions Updated"),
    ADMIN_SUSPENDED("Admin Suspended"),
    ADMIN_REACTIVATED("Admin Reactivated"),
    ADMIN_REVOKED("Admin Access Revoked")
}

data class AdminAuditLog(
    val id: String,
    val adminEmail: String,
    val action: AuditAction,
    val target: String,
    val timestampNepal: String,
    val details: String = ""
)

data class CsvScrimRow(
    val lineNumber: Int,
    val mode: ScrimMode,
    val map: ScrimMap,
    val entryFeeNpr: Int,
    val matchTimeNepal: String,
    val isValid: Boolean = true,
    val error: String = ""
)

data class CsvImportResult(
    val totalRowsParsed: Int,
    val validRows: List<CsvScrimRow>,
    val invalidRows: List<CsvScrimRow>
)

enum class ConversationType {
    DIRECT,
    GROUP,
    OFFICIAL_SUPPORT,
    ANNOUNCEMENTS
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val senderUid: String,
    val message: String,
    val timestampNepal: String,
    val isOfficialAdmin: Boolean = false,
    val isMe: Boolean = false,
    val replyToSender: String? = null,
    val attachedRoomData: String? = null,
    val status: MessageStatus = MessageStatus.READ
)

data class Conversation(
    val id: String,
    val title: String,
    val type: ConversationType,
    val lastMessage: String,
    val unreadCount: Int = 0,
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val groupMembersCount: Int = 0,
    val groupAdmins: List<String> = emptyList(),
    val isMuted: Boolean = false
)

data class SupportTicket(
    val id: String,
    val category: String,
    val description: String,
    val dateNepal: String,
    val status: String,
    val adminReply: String? = null
)
