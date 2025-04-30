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
data class CompletedWorkout(
    @PrimaryKey(autoGenerate = true) override var id: Long = 0,
    @ColumnInfo override var duration: Int,
    @ColumnInfo override val notes: String?,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime,
    @ColumnInfo(name = "workout_id") val workoutId: Long,
    @ColumnInfo(name = "exercises_number") var exercisesNumber: Int
): BaseCompletedWorkout()

@Dao
interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insert(completedWorkout: CompletedWorkout): Long

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    fun getById(completedWorkoutId: Long): Flow<CompletedWorkout?>

    @Delete
    suspend fun delete(completedWorkout: CompletedWorkout)

    @Update
    suspend fun update(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts")
    fun getAll(): Flow<List<CompletedWorkout>>
}

class CompletedWorkoutRepository(private val dao: CompletedWorkoutDao) {

    suspend fun insert(completedWorkout: CompletedWorkout): Long {
        return withContext(Dispatchers.IO) {
            dao.insert(completedWorkout)
        }
    }

    fun getById(completedWorkoutId: Long): Flow<CompletedWorkout?> {
        return dao.getById(completedWorkoutId)
    }

    suspend fun delete(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            dao.delete(completedWorkout)
        }
    }

    suspend fun update(completedWorkout: CompletedWorkout) {
        withContext(Dispatchers.IO) {
            dao.update(completedWorkout)
        }
    }
}