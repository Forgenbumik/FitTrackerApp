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
@RequiresApi(Build.VERSION_CODES.O)
data class CompletedWorkout(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo override val duration: Int = 0,
    @ColumnInfo override val notes: String? = null,
    @ColumnInfo(name = "begin_time") override val beginTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "workout_id") val workoutId: Long = 0
): BaseCompletedWorkout()

@Dao
interface CompletedWorkoutDao {
    @Insert(entity = CompletedWorkout::class)
    suspend fun insert(completedWorkout: CompletedWorkout): Long

    @Query("SELECT * FROM completed_workouts WHERE id = :completedWorkoutId")
    suspend fun getById(completedWorkoutId: Long): CompletedWorkout

    @Delete
    suspend fun delete(completedWorkout: CompletedWorkout)

    @Update
    suspend fun update(completedWorkout: CompletedWorkout)

    @Query("SELECT * FROM completed_workouts")
    suspend fun getAll(): List<CompletedWorkout>
}

class CompletedWorkoutRepository(private val dao: CompletedWorkoutDao) {

    suspend fun insert(completedWorkout: CompletedWorkout): Long {
        return withContext(Dispatchers.IO) {
            dao.insert(completedWorkout)
        }
    }

    suspend fun getById(completedWorkoutId: Long): CompletedWorkout {
        return withContext(Dispatchers.IO) {
            dao.getById(completedWorkoutId)
        }
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