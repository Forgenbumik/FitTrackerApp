package com.example.fittrackerapp.entities

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
data class CompletedExercise(
    @PrimaryKey(autoGenerate = true) override var id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo override var duration: Int,
    @ColumnInfo override val notes: String?,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long?,
    @ColumnInfo(name = "rest_duration") var restDuration : Int,
    @ColumnInfo(name = "total_reps") var totalReps: Int,
    @ColumnInfo(name = "sets_number") var setsNumber: Int
): BaseCompletedWorkout()

@Dao
interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    fun insert(completedExercise: CompletedExercise): Long

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    fun getById(completedExerciseId: Long): Flow<CompletedExercise>

    @Delete
    suspend fun delete(completedExercise: CompletedExercise)

    @Update
    suspend fun update(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises")
    fun getAll(): Flow<List<CompletedExercise>>

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    fun getByCompletedWorkoutId(completedWorkoutId: Long): Flow<List<CompletedExercise>>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): Exercise?
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

    fun getById(completedExerciseId: Long): Flow<CompletedExercise> {
        return dao.getById(completedExerciseId)
    }

    fun getAll(): Flow<List<CompletedExercise>> {
        return dao.getAll()
    }

    fun getByCompletedWorkoutId(completedWorkoutId: Long): Flow<List<CompletedExercise>> {
        return dao.getByCompletedWorkoutId(completedWorkoutId)
    }

    suspend fun getExerciseName(exerciseId: Long): String {
        return withContext(Dispatchers.IO) {
            val exercise = dao.getExerciseById(exerciseId)
            exercise?.name ?: ""
        }
    }
}