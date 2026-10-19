package com.example.data.firebase

import android.util.Log
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService {

    companion object {
        const val DATABASE_ID = "ai-studio-android-scrimsne-0afc29dd-7d79-4697-b52f-7b0e3923edb5"
        private const val TAG = "FirebaseService"
    }

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            FirebaseFirestore.getInstance(app, DATABASE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore instance for database $DATABASE_ID", e)
            try {
                FirebaseFirestore.getInstance()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fallback to default Firestore", ex)
                null
            }
        }
    }

    fun isCloudAvailable(): Boolean = firestore != null

    // Scrims Realtime Observation
    fun observeScrims(): Flow<List<Scrim>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection("scrims")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore scrims snapshot notice: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                Scrim(
                                    id = doc.id,
                                    map = try { ScrimMap.valueOf(data["map"] as? String ?: "BERMUDA") } catch (e: Exception) { ScrimMap.BERMUDA },
                                    mode = try { ScrimMode.valueOf(data["mode"] as? String ?: "SQUAD") } catch (e: Exception) { ScrimMode.SQUAD },
                                    status = try { ScrimStatus.valueOf(data["status"] as? String ?: "UPCOMING") } catch (e: Exception) { ScrimStatus.UPCOMING },
                                    entryFeeNpr = (data["entryFeeNpr"] as? Number)?.toInt() ?: 50,
                                    prizePoolNpr = (data["prizePoolNpr"] as? Number)?.toInt() ?: 1500,
                                    matchTimeNepal = data["matchTimeNepal"] as? String ?: "07:00 PM",
                                    matchDateFormatted = data["matchDateFormatted"] as? String ?: "Today",
                                    slotsTotal = (data["slotsTotal"] as? Number)?.toInt() ?: 12,
                                    slotsFilled = (data["slotsFilled"] as? Number)?.toInt() ?: 0,
                                    isFeatured = data["isFeatured"] as? Boolean ?: false,
                                    registeredUserIds = (data["registeredUserIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                    roomDetails = (data["roomDetails"] as? Map<*, *>)?.let { rm ->
                                        RoomDetails(
                                            roomId = rm["roomId"] as? String ?: "",
                                            roomPassword = rm["roomPassword"] as? String ?: "",
                                            instructions = rm["instructions"] as? String ?: "Join assigned slot.",
                                            isReleased = rm["isReleased"] as? Boolean ?: false,
                                            releaseTimeNepal = rm["releaseTimeNepal"] as? String ?: ""
                                        )
                                    } ?: RoomDetails()
                                )
                            } catch (e: Exception) {
                                Log.w(TAG, "Error mapping scrim doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(items)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error attaching scrims listener", e)
        }

        awaitClose {
            listener?.remove()
        }
    }

    suspend fun saveScrim(scrim: Scrim): Boolean {
        val db = firestore ?: return false
        return try {
            val data = hashMapOf(
                "id" to scrim.id,
                "map" to scrim.map.name,
                "mode" to scrim.mode.name,
                "status" to scrim.status.name,
                "entryFeeNpr" to scrim.entryFeeNpr,
                "prizePoolNpr" to scrim.prizePoolNpr,
                "matchTimeNepal" to scrim.matchTimeNepal,
                "matchDateFormatted" to scrim.matchDateFormatted,
                "slotsTotal" to scrim.slotsTotal,
                "slotsFilled" to scrim.slotsFilled,
                "isFeatured" to scrim.isFeatured,
                "registeredUserIds" to scrim.registeredUserIds,
                "roomDetails" to hashMapOf(
                    "roomId" to (scrim.roomDetails?.roomId ?: ""),
                    "roomPassword" to (scrim.roomDetails?.roomPassword ?: ""),
                    "instructions" to (scrim.roomDetails?.instructions ?: ""),
                    "isReleased" to (scrim.roomDetails?.isReleased ?: false),
                    "releaseTimeNepal" to (scrim.roomDetails?.releaseTimeNepal ?: "")
                )
            )
            db.collection("scrims").document(scrim.id).set(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save scrim ${scrim.id}", e)
            false
        }
    }

    suspend fun deleteScrim(scrimId: String): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection("scrims").document(scrimId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete scrim $scrimId", e)
            false
        }
    }

    // User Profile Realtime Observation
    fun observeUserProfile(uid: String): Flow<User?> = callbackFlow {
        val db = firestore
        if (db == null || uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "UserProfile snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val user = User(
                        id = snapshot.id,
                        email = data?.get("email") as? String ?: "",
                        displayName = data?.get("displayName") as? String ?: "Gamer",
                        freeFireIgn = data?.get("freeFireIgn") as? String ?: "",
                        freeFireUid = data?.get("freeFireUid") as? String ?: "",
                        walletBalanceNpr = (data?.get("walletBalanceNpr") as? Number)?.toInt() ?: 0,
                        totalMatchesPlayed = (data?.get("totalMatchesPlayed") as? Number)?.toInt() ?: 0,
                        totalWins = (data?.get("totalWins") as? Number)?.toInt() ?: 0,
                        role = data?.get("role") as? String ?: "USER",
                        createdAt = data?.get("createdAt") as? String ?: ""
                    )
                    trySend(user)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getUserProfile(uid: String): User? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val data = doc.data
                User(
                    id = doc.id,
                    email = data?.get("email") as? String ?: "",
                    displayName = data?.get("displayName") as? String ?: "Gamer",
                    freeFireIgn = data?.get("freeFireIgn") as? String ?: "",
                    freeFireUid = data?.get("freeFireUid") as? String ?: "",
                    walletBalanceNpr = (data?.get("walletBalanceNpr") as? Number)?.toInt() ?: 0,
                    totalMatchesPlayed = (data?.get("totalMatchesPlayed") as? Number)?.toInt() ?: 0,
                    totalWins = (data?.get("totalWins") as? Number)?.toInt() ?: 0,
                    role = data?.get("role") as? String ?: "USER",
                    createdAt = data?.get("createdAt") as? String ?: ""
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile $uid", e)
            null
        }
    }

    suspend fun createUserProfileIfAbsent(
        uid: String,
        email: String,
        displayName: String,
        ign: String,
        ffUid: String
    ): User {
        val db = firestore
        val existing = getUserProfile(uid)
        if (existing != null) {
            // Update IGN/UID if provided and missing
            if (ign.isNotBlank() && existing.freeFireIgn.isBlank()) {
                db?.collection("users")?.document(uid)?.update(
                    mapOf("freeFireIgn" to ign, "freeFireUid" to ffUid)
                )?.await()
                return existing.copy(freeFireIgn = ign, freeFireUid = ffUid)
            }
            return existing
        }

        val newUser = User(
            id = uid,
            email = email,
            displayName = displayName.ifBlank { "Player" },
            freeFireIgn = ign,
            freeFireUid = ffUid,
            walletBalanceNpr = 0, // Invariant: Initial balance must be 0 NPR
            totalMatchesPlayed = 0,
            totalWins = 0,
            role = if (email == "sagarpant391286@gmail.com") "ADMIN" else "USER",
            createdAt = System.currentTimeMillis().toString()
        )

        db?.collection("users")?.document(uid)?.set(
            hashMapOf(
                "id" to newUser.id,
                "email" to newUser.email,
                "displayName" to newUser.displayName,
                "freeFireIgn" to newUser.freeFireIgn,
                "freeFireUid" to newUser.freeFireUid,
                "walletBalanceNpr" to 0,
                "totalMatchesPlayed" to 0,
                "totalWins" to 0,
                "role" to newUser.role,
                "createdAt" to newUser.createdAt
            )
        )?.await()

        return newUser
    }

    suspend fun updateWalletBalance(uid: String, newBalance: Int): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection("users").document(uid).update("walletBalanceNpr", newBalance).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update wallet balance", e)
            false
        }
    }

    suspend fun addTransaction(uid: String, tx: WalletTransaction): Boolean {
        val db = firestore ?: return false
        return try {
            val data = hashMapOf(
                "id" to tx.id,
                "title" to tx.title,
                "amountNpr" to tx.amountNpr,
                "dateNepal" to tx.dateNepal,
                "status" to tx.status,
                "referenceId" to tx.referenceId,
                "type" to tx.type.name
            )
            db.collection("users").document(uid).collection("transactions").document(tx.id).set(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save transaction", e)
            false
        }
    }

    fun observeTransactions(uid: String): Flow<List<WalletTransaction>> = callbackFlow {
        val db = firestore
        if (db == null || uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid).collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Transactions snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            WalletTransaction(
                                id = doc.id,
                                title = data["title"] as? String ?: "",
                                amountNpr = (data["amountNpr"] as? Number)?.toInt() ?: 0,
                                dateNepal = data["dateNepal"] as? String ?: "",
                                status = data["status"] as? String ?: "COMPLETED",
                                referenceId = data["referenceId"] as? String ?: "",
                                type = try { TransactionType.valueOf(data["type"] as? String ?: "DEPOSIT_CONFIRMED") } catch (e: Exception) { TransactionType.DEPOSIT_CONFIRMED }
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }
}
