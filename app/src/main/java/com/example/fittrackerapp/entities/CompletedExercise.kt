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
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo override var duration: Int,
    @ColumnInfo override val notes: String?,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long?,
    @ColumnInfo(name = "rest_duration") var restDuration : Int
): BaseCompletedWorkout()

@Dao
interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    fun insert(completedExercise: CompletedExercise): Long

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getById(completedExerciseId: Long): CompletedExercise?

    @Delete
    suspend fun delete(completedExercise: CompletedExercise)

    @Update
    suspend fun update(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises")
    suspend fun getAll(): List<CompletedExercise>
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

    suspend fun getById(completedExerciseId: Long): CompletedExercise? {
        return withContext(Dispatchers.IO) {
            dao.getById(completedExerciseId)
        }
    }

    suspend fun getAll(): List<CompletedExercise> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}