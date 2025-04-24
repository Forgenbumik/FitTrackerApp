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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    @ColumnInfo(name = "completed_workout_id") val completedWorkoutId: Long,
    @ColumnInfo(name = "workout_name") val workoutName: String,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo val duration: Int
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

class LastWorkoutRepository(private val lastWorkoutDao: LastWorkoutDao,
                            private val workoutDao: WorkoutDao,
                            private val exerciseDao: ExerciseDao
) {

    suspend fun getLastWorkouts(): List<LastWorkout> {
        return withContext(Dispatchers.IO) {
            lastWorkoutDao.getAll()
        }
    }

    suspend fun insertLastWorkout(baseCompletedWorkout: BaseCompletedWorkout): Long {
        val lastWorkouts = getLastWorkouts().toMutableList()
        if (lastWorkouts.size == 3) {
            lastWorkouts.removeAt(0)
        }
        val workout: LastWorkout
        when (baseCompletedWorkout) {
            is CompletedWorkout -> {
                val completedWorkout = baseCompletedWorkout
                val baseWorkout = workoutDao.getById(completedWorkout.workoutId)
                if (baseWorkout != null) {
                    workout = LastWorkout(0, completedWorkout.id, baseWorkout.name, 1, completedWorkout.duration)
                    return lastWorkoutDao.insert(workout)
                }
            }
            is CompletedExercise -> {
                val completedExercise = baseCompletedWorkout
                val exercise = exerciseDao.getById(completedExercise.exerciseId)
                if (exercise != null) {
                    workout = LastWorkout(0, completedExercise.id, exercise.name, 2, completedExercise.duration)
                    return lastWorkoutDao.insert(workout)
                }
            }
        }
        return 0

    }
}