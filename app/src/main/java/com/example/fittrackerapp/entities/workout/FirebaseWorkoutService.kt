package com.example.fittrackerapp.entities.workout

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseWorkoutService(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {
    fun upload(workout: Workout): Task<Void> {
        if (workout.userId == "") {
            val workoutToUpload = workout.copy(userId = userId)
            return firestore
                .collection("workouts")
                .document(workoutToUpload.id)
                .set(workoutToUpload)
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
            .collection("workouts")
            .document(workout.id)
            .set(workout)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(workout: Workout): Task<Void> {

        if (workout.id.isEmpty()) {
            throwSQLiteException(1, "Cannot delete workout with empty id")
        }

        return firestore
            .collection("workouts")
            .document(workout.id)
            .delete()
    }

    suspend fun getWorkoutsForUserOrDefault(): List<Workout> {
        val userSnapshot = firestore
            .collection("workouts")
            .whereEqualTo("user_id", userId)
            .get()
            .await()

        val defaultSnapshot = firestore
            .collection("workouts")
            .whereEqualTo("user_id", null)
            .get()
            .await()

        val userWorkouts = userSnapshot.toObjects(Workout::class.java)
        val defaultWorkouts = defaultSnapshot.toObjects(Workout::class.java)

        return userWorkouts + defaultWorkouts
    }
}