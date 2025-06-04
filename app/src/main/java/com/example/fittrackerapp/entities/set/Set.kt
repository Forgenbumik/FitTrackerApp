package com.example.fittrackerapp.entities.set

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.workout.Workout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = CompletedExercise::class,
            parentColumns = ["id"],
            childColumns = ["completed_exercise_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Set(
    @PrimaryKey(autoGenerate = false) val id: String = "",
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: String = "",
    @ColumnInfo(name = "duration") val duration: Int = 0,
    @ColumnInfo(name = "reps") val reps: Int = 0,
    @ColumnInfo(name = "weight") val weight: Double = 0.0,
    @ColumnInfo(name = "rest_duration") val restDuration: Int = 0,
    @ColumnInfo(name = "set_number") val setNumber: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String = ""
)

@Dao
interface SetDao {

    @Insert(entity = Set::class)
    suspend fun insert(set: Set)

    @Query("SELECT * FROM sets WHERE id = :setId")
    suspend fun getById(setId: String): Set

    @Delete
    suspend fun delete(set: Set)

    @Update
    suspend fun update(set: Set)

    @Query("UPDATE sets SET rest_duration = :restDuration WHERE id = :id")
    suspend fun updateRestDuration(id: String, restDuration: Int)

    @Query("SELECT * FROM sets WHERE completed_exercise_id = :completedExerciseId")
    suspend fun getByCompletedExerciseId(completedExerciseId: String): List<Set>

    @Query("SELECT * FROM sets WHERE completed_exercise_id = :completedExerciseId")
    fun getByCompletedExerciseIdFlow(completedExerciseId: String): Flow<List<Set>>

    @Insert
    suspend fun insertAll(sets: List<Set>)
}

@Singleton
class SetRepository @Inject constructor(private val dao: SetDao,
    private val firestore: FirebaseSetService
) {

    suspend fun insert(set: Set) {
        withContext(Dispatchers.IO) {
            firestore.upload(set)
            dao.insert(set)
        }
    }

    suspend fun getById(setId: String): Set {
        return withContext(Dispatchers.IO) {
            dao.getById(setId)
        }
    }

    suspend fun delete(set: Set) {
        withContext(Dispatchers.IO) {
            firestore.delete(set)
            dao.delete(set)
        }
    }

    suspend fun update(set: Set) {
        withContext(Dispatchers.IO) {
            firestore.upload(set)
            dao.update(set)
        }
    }

    suspend fun getByCompletedExerciseId(completedExerciseId: String): List<Set> {
        return withContext(Dispatchers.IO) {
            dao.getByCompletedExerciseId(completedExerciseId)
        }
    }

    fun getByCompletedExerciseIdFlow(completedExerciseId: String): Flow<List<Set>> {
        return dao.getByCompletedExerciseIdFlow(completedExerciseId)
    }
}