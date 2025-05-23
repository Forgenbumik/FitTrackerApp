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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(
    tableName = "workout_details",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutDetail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo val position: Int,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "exercise_name") val exerciseName: String,
    @ColumnInfo(name = "sets_number") val setsNumber: Int,
    @ColumnInfo val reps: Int,
    @ColumnInfo(name = "rest_duration") val restDuration: Int,
    @ColumnInfo(name = "is_rest_manually") val isRestManually: Boolean
)

@Dao
interface WorkoutDetailDao {
    @Insert(entity = WorkoutDetail::class)
    suspend fun insert(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE id = :workoutDetailId")
    suspend fun getById(workoutDetailId: Long): WorkoutDetail?

    @Delete
    suspend fun delete(workoutDetail: WorkoutDetail)

    @Update
    suspend fun update(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE workout_id = :workoutId")
    suspend fun getByWorkout(workoutId: Long): List<WorkoutDetail>
}

class WorkoutDetailRepository(private val dao: WorkoutDetailDao) {

    suspend fun insert(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.insert(workoutDetail)
        }
    }

    suspend fun getById(workoutDetailId: Long): WorkoutDetail? {
        return withContext(Dispatchers.IO) {
            dao.getById(workoutDetailId)
        }
    }

    suspend fun delete(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.delete(workoutDetail)
        }
    }

    suspend fun update(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            dao.update(workoutDetail)
        }
    }

    suspend fun getByWorkoutId(workoutId: Long): List<WorkoutDetail> {
        return withContext(Dispatchers.IO) {
            dao.getByWorkout(workoutId)
        }
    }
}