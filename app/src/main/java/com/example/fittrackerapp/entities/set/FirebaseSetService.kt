package com.example.fittrackerapp.entities.set

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirebaseSetService(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    fun upload(set: Set): Task<Void> {

        if (set.userId == "") {
            val setToUpload = set.copy(userId = userId)
            return firestore
                .collection("sets")
                .document(setToUpload.id)
                .set(setToUpload)
                .addOnSuccessListener {
                    Log.d("Firestore", "Insert successful")
                    // Здесь вставка прошла успешно
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Insert failed", e)
                    // Здесь была ошибка
                }
        }

        return firestore
            .collection("sets")
            .document(set.id)
            .set(set)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(set: Set): Task<Void> {

        if (set.id.isEmpty()) {
            throwSQLiteException(1, "Cannot delete workout with empty id")
        }

        return firestore
            .collection("sets")
            .document(set.id)
            .delete().addOnSuccessListener {
                Log.d("Firebase", "Delete successful")
            }
    }

    suspend fun getAll(): QuerySnapshot {
        return firestore
            .collection("sets")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
    }
}