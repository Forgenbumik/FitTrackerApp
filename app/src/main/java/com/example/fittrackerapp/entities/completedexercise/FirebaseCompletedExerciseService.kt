package com.example.fittrackerapp.entities.completedexercise

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.example.fittrackerapp.entities.workout.Workout
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


class FirebaseCompletedExerciseService(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    fun upload(completedExercise: CompletedExercise): Task<Void> {

        if (completedExercise.userId == "") {
            val completedExerciseToUpload = completedExercise.copy(userId = userId)
            return firestore
                .collection("completed_exercises")
                .document(completedExerciseToUpload.id)
                .set(completedExerciseToUpload)
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
            .collection("completed_exercises")
            .document(completedExercise.id)
            .set(completedExercise)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(completedExercise: CompletedExercise): Task<Void> {
        if (completedExercise.id.isEmpty()) {
            throwSQLiteException(1, "Cannot delete workout with empty id")
        }

        return firestore
            .collection("completed_exercises")
            .document(completedExercise.id)
            .delete()
    }

    suspend fun getAll(): QuerySnapshot {

        return firestore
            .collection("completed_exercises")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
    }
}