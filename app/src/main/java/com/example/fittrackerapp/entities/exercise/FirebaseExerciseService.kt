package com.example.fittrackerapp.entities.exercise

import android.util.Log
import androidx.sqlite.throwSQLiteException
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirebaseExerciseService(
    private val firestore: FirebaseFirestore,
    private val userId: String
) {

    fun upload(exercise: Exercise): Task<Void> {

        if (exercise.userId == "") {
            val exerciseToUpload = exercise.copy(userId = userId)
            return firestore
                .collection("exercises")
                .document(exerciseToUpload.id)
                .set(exerciseToUpload)
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
            .collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .addOnSuccessListener {
                Log.d("Firestore", "Insert successful")
                // Здесь вставка прошла успешно
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Insert failed", e)
                // Здесь была ошибка
            }
    }

    fun delete(exercise: Exercise): Task<Void> {

        if (exercise.id.isEmpty()) {
            throwSQLiteException(1, "Cannot delete workout with empty id")
        }

        return firestore
            .collection("exercises")
            .document(exercise.id)
            .delete()
    }

    suspend fun getExercisesForUserOrDefault(): List<Exercise> {
        val userSnapshot = firestore
            .collection("exercises")
            .whereEqualTo("user_id", userId)
            .get()
            .await()

        val defaultSnapshot = firestore
            .collection("exercises")
            .whereEqualTo("user_id", null)
            .get()
            .await()

        val userExercises = userSnapshot.toObjects(Exercise::class.java)
        val defaultExercises = defaultSnapshot.toObjects(Exercise::class.java)

        return userExercises + defaultExercises
    }
}