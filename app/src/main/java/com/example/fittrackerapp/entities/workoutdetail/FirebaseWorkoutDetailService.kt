package com.example.fittrackerapp.entities.workoutdetail

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirebaseWorkoutDetailService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userId: String
) {

    fun upload(workoutDetail: WorkoutDetail): Task<Void> {

        if (workoutDetail.userId == "") {
            val workoutDetailToUpload = workoutDetail.copy(userId = userId)
            return firestore
                .collection("workout_details")
                .document(workoutDetailToUpload.id)
                .set(workoutDetailToUpload)
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
            .collection("workout_details")
            .document(workoutDetail.id)
            .set(workoutDetail)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(workoutDetail: WorkoutDetail): Task<Void> {
        return firestore
            .collection("workout_details")
            .document(workoutDetail.id)
            .delete()
    }

    suspend fun getAll(): QuerySnapshot {
        return firestore
            .collection("workout_details")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
    }
}