package com.example.fittrackerapp.entities.workoutdetail

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.workout.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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
    @PrimaryKey(autoGenerate = false) val id: String = "",
    @ColumnInfo val position: Int = 0,
    @ColumnInfo(name = "workout_id") val workoutId: String = "",
    @ColumnInfo(name = "exercise_id") val exerciseId: String = "",
    @ColumnInfo(name = "sets_number") val setsNumber: Int = 0,
    @ColumnInfo val reps: Int = 0,
    @ColumnInfo(name = "rest_duration") val restDuration: Int = 0,
    @ColumnInfo(name = "is_rest_manually") val isRestManually: Boolean = false,
    @ColumnInfo(name = "user_id") val userId: String = ""
)

@Dao
interface WorkoutDetailDao {
    @Insert(entity = WorkoutDetail::class)
    suspend fun insert(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE id = :workoutDetailId")
    suspend fun getById(workoutDetailId: String): WorkoutDetail

    @Delete
    suspend fun delete(workoutDetail: WorkoutDetail)

    @Update
    suspend fun update(workoutDetail: WorkoutDetail)

    @Query("SELECT * FROM workout_details WHERE workout_id = :workoutId")
    suspend fun getByWorkout(workoutId: String): List<WorkoutDetail>

    @Query("SELECT * FROM workout_details WHERE workout_id = :workoutId")
    fun getByWorkoutFlow(workoutId: String): Flow<List<WorkoutDetail>>

    @Query("DELETE from workout_details where exercise_id = :exerciseId")
    fun deleteExerciseFromWorkout(exerciseId: String)

    @Insert
    suspend fun insertAll(workoutDetails: List<WorkoutDetail>)
}

@Singleton
class WorkoutDetailRepository @Inject constructor(
    private val dao: WorkoutDetailDao,
    private val firebaseSource: FirebaseWorkoutDetailService) {

    suspend fun insert(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            firebaseSource.upload(workoutDetail)
            dao.insert(workoutDetail)
        }
    }

    suspend fun getById(workoutDetailId: String): WorkoutDetail {
        return withContext(Dispatchers.IO) {
            dao.getById(workoutDetailId)
        }
    }

    suspend fun delete(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            firebaseSource.delete(workoutDetail)
            dao.delete(workoutDetail)
        }
    }

    suspend fun update(workoutDetail: WorkoutDetail) {
        withContext(Dispatchers.IO) {
            firebaseSource.upload(workoutDetail)
            dao.update(workoutDetail)
        }
    }

    suspend fun getByWorkoutId(workoutId: String): List<WorkoutDetail> {
        return withContext(Dispatchers.IO) {
            dao.getByWorkout(workoutId)
        }
    }

    suspend fun getByWorkoutIdFlow(workoutId: String): Flow<List<WorkoutDetail>> {
        return withContext(Dispatchers.IO) {
            dao.getByWorkoutFlow(workoutId)
        }
    }
}