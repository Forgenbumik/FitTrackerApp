package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.workout.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "last_workouts",
    foreignKeys = [
        ForeignKey(
            entity = CompletedWorkout::class,
            parentColumns = ["id"],
            childColumns = ["completed_workout_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Type::class,
            parentColumns = ["id"],
            childColumns = ["type_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ])
data class LastWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: String = "",
    @ColumnInfo(name = "workout_name") val workoutName: String = "",
    @ColumnInfo(name = "type_id") val typeId: Int = 0,
    @ColumnInfo val duration: Int = 0
)

@Dao
interface LastWorkoutDao {

    @Insert(entity = LastWorkout::class)
    suspend fun insert(lastWorkout: LastWorkout): Long

    @Query("SELECT * FROM last_workouts")
    suspend fun getAll(): List<LastWorkout>

    @Delete
    suspend fun delete(lastWorkout: LastWorkout)

    @Query("SELECT * FROM last_workouts WHERE id = :id")
    suspend fun getById(id: Int): LastWorkout
}

@Singleton
class LastWorkoutRepository @Inject constructor(private val lastWorkoutDao: LastWorkoutDao,
                            private val workoutDao: WorkoutDao,
                            private val exerciseDao: ExerciseDao
) {

    suspend fun getLastWorkouts(): List<LastWorkout> {
        return withContext(Dispatchers.IO) {
            lastWorkoutDao.getAll()
        }
    }

    suspend fun delete(lastWorkout: LastWorkout) {
        lastWorkoutDao.delete(lastWorkout)
    }

    suspend fun insertLastWorkout(baseCompletedWorkout: BaseCompletedWorkout): Long {
        val lastWorkouts = getLastWorkouts().toMutableList()
        if (lastWorkouts.size == 3) {
            delete(lastWorkouts.get(0))
        }
        val workout: LastWorkout
        when (baseCompletedWorkout) {
            is CompletedWorkout -> {
                val completedWorkout = baseCompletedWorkout
                val baseWorkout = workoutDao.getById(completedWorkout.workoutId)
                workout = LastWorkout(0, completedWorkout.id, baseWorkout.name, 1, completedWorkout.duration)
                return lastWorkoutDao.insert(workout)
            }
            is CompletedExercise -> {
                val exercise = exerciseDao.getById(baseCompletedWorkout.exerciseId)
                workout = LastWorkout(
                    0,
                    baseCompletedWorkout.id,
                    exercise.name,
                    2,
                    baseCompletedWorkout.duration
                )
                return lastWorkoutDao.insert(workout)
            }
        }
        return 0
    }
}