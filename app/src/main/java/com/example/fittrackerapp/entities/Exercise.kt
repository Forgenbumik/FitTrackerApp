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
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) override val id: Long = 0,
    @ColumnInfo override val name: String,
    @ColumnInfo(name = "is_used") override var isUsed: Boolean,
    @ColumnInfo(name = "is_user_defined") val isUserDefined: Boolean,
    @ColumnInfo(name = "last_used_date") override val lastUsedDate: LocalDateTime
): BaseWorkout()

@Dao
interface ExerciseDao {

    @Insert(entity = Exercise::class)
    suspend fun insert(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getById(exerciseId: Long): Exercise?

    @Delete
    suspend fun delete(exercise: Exercise)

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercises where is_used = 1")
    suspend fun getAllExceptNotUsed(): List<Exercise>

    @Query("SELECT name FROM exercises")
    suspend fun getAllExerciseNames(): List<String>
}

class ExerciseRepository(private val dao: ExerciseDao) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(name: String) {
        val exercise = Exercise(0, name, true, true, LocalDateTime.now())
        return withContext(Dispatchers.IO) {
            dao.insert(exercise)
        }
    }

    suspend fun getById(exerciseId: Long): Exercise? {
        return withContext(Dispatchers.IO) {
            dao.getById(exerciseId)
        }
    }

    suspend fun delete(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            dao.delete(exercise)
        }
    }

    suspend fun update(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            dao.update(exercise)
        }
    }

    suspend fun getAllExceptAdded(): List<Exercise> {
        return withContext(Dispatchers.IO) {
            dao.getAllExceptNotUsed()
        }
    }

    suspend fun getAllExerciseNames(): List<String> {
        return withContext(Dispatchers.IO) {
            dao.getAllExerciseNames()
        }
    }
}