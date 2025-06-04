package com.example.fittrackerapp.entities.completedexercise

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
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.workout.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

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
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
@RequiresApi(Build.VERSION_CODES.O)
data class CompletedExercise (
    @PrimaryKey(autoGenerate = false) override val id: String = "",
    @ColumnInfo(name = "exercise_id") val exerciseId: String = "",
    @ColumnInfo override val duration: Int = 0,
    @ColumnInfo override val notes: String? = null,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: String? = null,
    @ColumnInfo(name = "rest_duration") val restDuration : Int = 0,
    @ColumnInfo(name = "user_id") val userId: String = ""
): BaseCompletedWorkout()

@Dao
interface CompletedExerciseDao {

    @Insert(entity = CompletedExercise::class)
    fun insert(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises WHERE id = :completedExerciseId")
    suspend fun getById(completedExerciseId: String): CompletedExercise

    @Delete
    suspend fun delete(completedExercise: CompletedExercise)

    @Update
    suspend fun update(completedExercise: CompletedExercise)

    @Query("SELECT * FROM completed_exercises")
    suspend fun getAll(): List<CompletedExercise>

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    fun getByCompletedWorkoutIdFlow(completedWorkoutId: String): Flow<List<CompletedExercise>>

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = :completedWorkoutId")
    suspend fun getByCompletedWorkoutId(completedWorkoutId: String): List<CompletedExercise>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): Exercise

    @Query("SELECT COUNT(*) from sets WHERE completed_exercise_id = :exerciseId")
    suspend fun getSetsNumber(exerciseId: String): Int

    @Query("SELECT SUM(reps) from sets WHERE completed_exercise_id = :exerciseId")
    suspend fun getTotalReps(exerciseId: String): Int

    @Query("SELECT icon_path FROM exercises WHERE id = ( SELECT exercise_id FROM completed_exercises WHERE id = :completedExerciseId)")
    suspend fun getExerciseIconPath(completedExerciseId: String): String?

    @Query("SELECT * FROM completed_exercises WHERE completed_workout_id = null")
    suspend fun getSeparateCompleted(): List<CompletedExercise>

    @Insert
    suspend fun insertAll(completedExercises: List<CompletedExercise>)

    @Query("SELECT * from completed_exercises WHERE completed_workout_id = null")
    fun getSeparateCompletedFlow(): Flow<List<CompletedExercise>>
}

@Singleton
class CompletedExerciseRepository @Inject constructor(
    private val dao: CompletedExerciseDao,
    private val firebaseSource: FirebaseCompletedExerciseService,
    ) {

    suspend fun insert(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            firebaseSource.upload(completedExercise)
            dao.insert(completedExercise)
        }
    }

    suspend fun getExerciseIconPath(completedExerciseId: String): String? {
        return withContext(Dispatchers.IO) {
            dao.getExerciseIconPath(completedExerciseId)
        }
    }

    suspend fun delete(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            firebaseSource.delete(completedExercise)
            dao.delete(completedExercise)
        }
    }

    suspend fun update(completedExercise: CompletedExercise) {
        withContext(Dispatchers.IO) {
            firebaseSource.upload(completedExercise)
            dao.update(completedExercise)
        }
    }

    suspend fun getById(completedExerciseId: String): CompletedExercise {
        return withContext(Dispatchers.IO) {
            dao.getById(completedExerciseId)
        }
    }

    suspend fun getAll(): List<CompletedExercise> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }

    fun getByCompletedWorkoutIdFlow(completedWorkoutId: String): Flow<List<CompletedExercise>> {
        return dao.getByCompletedWorkoutIdFlow(completedWorkoutId)
    }

    suspend fun getByCompletedWorkoutId(completedWorkoutId: String): List<CompletedExercise> {
        return withContext(Dispatchers.IO) {
            dao.getByCompletedWorkoutId(completedWorkoutId)
        }
    }

    suspend fun getSetsNumber(exerciseId: String): Int {
        return withContext(Dispatchers.IO) {
            dao.getSetsNumber(exerciseId)
        }
    }

    suspend fun getTotalReps(exerciseId: String): Int {
        return withContext(Dispatchers.IO) {
            dao.getTotalReps(exerciseId)
        }
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return withContext(Dispatchers.IO) {
            val exercise = dao.getExerciseById(exerciseId)
            exercise.name
        }
    }
}