package com.example.fittrackerapp.entities.completedworkout

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
import com.example.fittrackerapp.entities.workout.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Entity(
    tableName = "completed_workouts",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
@RequiresApi(Build.VERSION_CODES.O)
data class CompletedWorkout(
    @PrimaryKey(autoGenerate = false) override val id: String = "",
    @ColumnInfo override val duration: Int = 0,
    @ColumnInfo override val notes: String? = null,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "workout_id") val workoutId: String = "",
    @ColumnInfo(name = "user_id") val userId: String = ""
): BaseCompletedWorkout()

@Dao
interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insert(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    suspend fun getById(completedWorkoutId: String): CompletedWorkout

    @Delete
    suspend fun delete(completedWorkout: CompletedWorkout)

    @Update
    suspend fun update(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts")
    suspend fun getAll(): List<CompletedWorkout>

    @Query("SELECT * FROM completed_workouts")
    fun getAllFlow(): Flow<List<CompletedWorkout>>

    @Query("SELECT name FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutName(workoutId: String): String

    @Insert
    suspend fun insertAll(completedWorkouts: List<CompletedWorkout>)
}

@Singleton
class CompletedWorkoutRepository @Inject constructor(
    private val dao: CompletedWorkoutDao,
    private val firebaseSource: FirebaseCompletedWorkoutService) {

    suspend fun insert(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            //firebaseSource.upload(completedWorkout)
            dao.insert(completedWorkout)
        }
    }

    suspend fun getById(completedWorkoutId: String): CompletedWorkout {
        return withContext(Dispatchers.IO) {
            dao.getById(completedWorkoutId)
        }
    }

    suspend fun delete(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            //firebaseSource.delete(completedWorkout)
            dao.delete(completedWorkout)
        }
    }

    suspend fun update(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            //firebaseSource.upload(completedWorkout)
            dao.update(completedWorkout)
        }
    }

    fun getAllFlow(): Flow<List<CompletedWorkout>> {
        return dao.getAllFlow()
    }

    suspend fun getWorkoutName(workoutId: String): String {
        return withContext(Dispatchers.IO) {
            dao.getWorkoutName(workoutId)
        }
    }
}