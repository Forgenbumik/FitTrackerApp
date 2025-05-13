package com.example.fittrackerapp.entities

import android.os.Build
import androidx.annotation.RequiresApi
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
@RequiresApi(Build.VERSION_CODES.O)
data class Workout(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo override var name: String = "",
    @ColumnInfo(name = "is_user_defined") val isUserDefined: Boolean = true,
    @ColumnInfo(name = "is_used") override val isUsed: Boolean = true,
    @ColumnInfo(name = "last_used_date") override val lastUsedDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "is_favourite") override val isFavourite: Boolean = false,
    @ColumnInfo(name = "is_deleted") override val isDeleted: Boolean = false
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

    @Query("SELECT * FROM workouts where is_deleted = 0")
    suspend fun getAll(): List<Workout>

    @Query("SELECT name FROM workouts")
    suspend fun getWorkoutsNames(): List<String>

    @Query("SELECT * FROM workouts WHERE is_favourite = 1 AND is_used = 1")
    fun getFavouritesFlow(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE is_favourite = 0 AND is_used = 1")
    fun getUsedExceptFavouritesFlow(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE is_favourite = 0 AND is_used = 1")
    suspend fun getUsedExceptFavourites(): List<Workout>

    @Query("SELECT * from workouts where is_deleted = 0 and is_used = 0")
    suspend fun getNotUsed(): List<Workout>
}

class WorkoutRepository(private val dao: WorkoutDao) {
    suspend fun insert(workout: Workout) {
        withContext(Dispatchers.IO) {
            dao.insert(workout)
        }
    }


    suspend fun getNotUsed(): List<Workout> {
        return withContext(Dispatchers.IO) {
            dao.getNotUsed()
        }
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

    suspend fun delete(workout: Workout) {
        withContext(Dispatchers.IO) {
            dao.delete(workout)
        }
    }

    suspend fun update(workout: Workout) {
        withContext(Dispatchers.IO) {
            dao.update(workout)
        }
    }


}