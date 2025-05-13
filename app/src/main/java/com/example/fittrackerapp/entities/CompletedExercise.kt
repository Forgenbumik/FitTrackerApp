package com.example.fittrackerapp.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Entity(
    tableName = "completed_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CompletedWorkout::class,
            parentColumns = ["id"],
            childColumns = ["completed_workout_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)

@RequiresApi(Build.VERSION_CODES.O)
data class CompletedExercise(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long = 0,
    @ColumnInfo override val duration: Int = 0,
    @ColumnInfo override val notes: String? = null,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long? = null,
    @ColumnInfo(name = "rest_duration") val restDuration : Int = 0
): BaseCompletedWorkout()

@Dao
interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    fun insert(completedExercise: CompletedExercise): Long

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getById(completedExerciseId: Long): CompletedExercise

    @Delete
    suspend fun delete(completedExercise: CompletedExercise)

    @Update
    suspend fun update(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises")
    suspend fun getAll(): List<CompletedExercise>

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    fun getByCompletedWorkoutIdFlow(completedWorkoutId: Long): Flow<List<CompletedExercise>>

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    suspend fun getByCompletedWorkoutId(completedWorkoutId: Long): List<CompletedExercise>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): Exercise

    @Query("SELECT COUNT(*) from sets WHERE completed_exercise_id = :exerciseId")
    suspend fun getSetsNumber(exerciseId: Long): Int

    @Query("SELECT SUM(reps) from sets WHERE completed_exercise_id = :exerciseId")
    suspend fun getTotalReps(exerciseId: Long): Int
}

class CompletedExerciseRepository(private val dao: CompletedExerciseDao) {

    suspend fun insert(completedExercise: CompletedExercise): Long {
        return withContext(Dispatchers.IO) {
            dao.insert(completedExercise)
        }
    }

    suspend fun delete(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            dao.delete(completedExercise)
        }
    }

    suspend fun update(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            dao.update(completedExercise)
        }
    }

    suspend fun getById(completedExerciseId: Long): CompletedExercise {
        return withContext(Dispatchers.IO) {
            dao.getById(completedExerciseId)
        }
    }

    suspend fun getAll(): List<CompletedExercise> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }

    fun getByCompletedWorkoutIdFlow(completedWorkoutId: Long): Flow<List<CompletedExercise>> {
        return dao.getByCompletedWorkoutIdFlow(completedWorkoutId)
    }

    suspend fun getByCompletedWorkoutId(completedWorkoutId: Long): List<CompletedExercise> {
        return withContext(Dispatchers.IO) {
            dao.getByCompletedWorkoutId(completedWorkoutId)
        }
    }

    suspend fun getSetsNumber(exerciseId: Long): Int {
        return withContext(Dispatchers.IO) {
            dao.getSetsNumber(exerciseId)
        }
    }

    suspend fun getTotalReps(exerciseId: Long): Int {
        return withContext(Dispatchers.IO) {
            dao.getTotalReps(exerciseId)
        }
    }

    suspend fun getExerciseName(exerciseId: Long): String {
        return withContext(Dispatchers.IO) {
            val exercise = dao.getExerciseById(exerciseId)
            exercise?.name ?: ""
        }
    }
}