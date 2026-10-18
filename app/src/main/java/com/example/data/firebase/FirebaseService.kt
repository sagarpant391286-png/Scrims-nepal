package com.example.data.firebase

import android.util.Log
import com.example.model.Scrim
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseService {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w("FirebaseService", "FirebaseApp not initialized, using local repository data", e)
            null
        }
    }

    fun isCloudAvailable(): Boolean = firestore != null

    fun observeScrims(): Flow<List<Scrim>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection("scrims")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseService", "Firestore snapshot error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Scrim::class.java)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(items)
                    }
                }
        } catch (e: Exception) {
            Log.w("FirebaseService", "Error attaching snapshot listener", e)
        }

        awaitClose {
            listener?.remove()
        }
    }
}
