package com.example.fittrackerapp.entities.completedworkout

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirebaseCompletedWorkoutService(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    fun upload(completedWorkout: CompletedWorkout): Task<Void> {

        if (completedWorkout.userId == "") {
            val completedWorkoutToUpload = completedWorkout.copy(userId = userId)
            return firestore
                .collection("completed_workouts")
                .document(completedWorkoutToUpload.id)
                .set(completedWorkoutToUpload)
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
            .collection("completed_workouts")
            .document(completedWorkout.id)
            .set(completedWorkout)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(completedWorkout: CompletedWorkout): Task<Void> {

        if (completedWorkout.id.isEmpty()) {
            throwSQLiteException(1, "Cannot delete workout with empty id")
        }

        return firestore
            .collection("completed_workouts")
            .document(completedWorkout.id)
            .delete()
    }

    suspend fun getAll(): QuerySnapshot {

        return firestore
            .collection("completed_workouts")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
    }
}