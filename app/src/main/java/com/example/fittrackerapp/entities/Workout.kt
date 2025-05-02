package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Entity(tableName = "workouts")
class Workout(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo override val name: String,
    @ColumnInfo(name = "is_user_defined") val isUserDefined: Boolean,
    @ColumnInfo(name = "is_used") override val isUsed: Boolean,
    @ColumnInfo(name = "last_used_date") override val lastUsedDate: LocalDateTime
): BaseWorkout()

@Dao
interface WorkoutDao {

    @Insert(entity = Workout::class)
    suspend fun insert(workout: Workout): Long

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Long): Workout?

    @Delete
    suspend fun delete(workout: Workout)

    @Update
    suspend fun update(workout: Workout)

    @Query("SELECT * FROM workouts")
    fun getAllFlow(): Flow<List<Workout>>

    @Query("SELECT name FROM workouts")
    suspend fun getWorkoutsNames(): List<String>
}

class WorkoutRepository(private val dao: WorkoutDao) {
    suspend fun insert(workout: Workout) {
        withContext(Dispatchers.IO) {
            dao.insert(workout)
        }
    }

    fun getAllFlow(): Flow<List<Workout>> {
        return dao.getAllFlow()
    }

    suspend fun getById(id: Long): Workout? {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    suspend fun getWorkoutsNames(): List<String> {
        return withContext(Dispatchers.IO) {
            dao.getWorkoutsNames()
        }
    }
}