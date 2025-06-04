package com.example.fittrackerapp.entities.exercise

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
import com.example.fittrackerapp.entities.workout.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "exercises")
@RequiresApi(Build.VERSION_CODES.O)
data class Exercise(
    @PrimaryKey(autoGenerate = false) override val id: String = "",
    @ColumnInfo override var name: String = "",
    @ColumnInfo(name = "is_used") override val isUsed: Boolean = true,
    @ColumnInfo(name = "is_user_defined") val isUserDefined: Boolean = true,
    @ColumnInfo(name = "last_used_date") override val lastUsedDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "is_favourite") override val isFavourite: Boolean = false,
    @ColumnInfo(name = "is_deleted") override val isDeleted: Boolean = false,
    @ColumnInfo(name = "icon_path") val iconPath: String? = null,
    @ColumnInfo(name = "video_path") val videoPath: String? = null,
    @ColumnInfo(name = "user_id") val userId: String? = null
): BaseWorkout()

@Dao
interface ExerciseDao {

    @Insert(entity = Exercise::class)
    suspend fun insert(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    fun getByIdFlow(exerciseId: String): Flow<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getById(exerciseId: String): Exercise

    @Delete
    suspend fun delete(exercise: Exercise)

    @Update
    suspend fun update(exercise: Exercise)

    @Query("SELECT * FROM exercises where is_used = 1 and is_favourite = 0 and is_deleted = 0")
    fun getUsedExceptFavouritesFlow(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises where is_used = 1 and is_favourite = 0 and is_deleted = 0")
    fun getUsedExceptFavourites(): List<Exercise>

    @Query("SELECT * FROM exercises where is_used = 1 and is_deleted = 0")
    fun getUsed(): List<Exercise>

    @Query("SELECT * FROM exercises where is_favourite = 1  and is_deleted = 0")
    fun getFavouritesFlow(): Flow<List<Exercise>>

    @Query("SELECT name FROM exercises where is_deleted = 0")
    fun getAllExerciseNamesFlow(): Flow<List<String>>

    @Query("SELECT name FROM exercises where is_deleted = 0")
    fun getAllExerciseNames(): List<String>

    @Query("SELECT name FROM exercises where id = :exerciseId")
    fun getExerciseName(exerciseId: String): String

    @Query("SELECT * from exercises where is_deleted = 0")
    fun getAllFlow(): Flow<List<Exercise>>

    @Query("SELECT * from exercises where is_deleted = 0")
    suspend fun getAll(): List<Exercise>

    @Query("UPDATE exercises SET is_used = :isUsed where id = :exerciseId")
    suspend fun updateExerciseUsingById(exerciseId: String, isUsed: Boolean)

    @Query("SELECT * from exercises where is_used = 0 and is_deleted = 0")
    suspend fun getNotUsed(): List<Exercise>

    @Query("SELECT video_path from exercises where id = :exerciseId")
    suspend fun getVideoPath(exerciseId: String): String?

    @Insert
    suspend fun insertAll(exercises: List<Exercise>)
}

@Singleton
class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao,
    private val service: FirebaseExerciseService) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(name: String) {
        val exercise = Exercise(name = name)
        withContext(Dispatchers.IO) {
            service.upload(exercise)
            dao.insert(exercise)
        }
    }

    suspend fun insert(exercise: Exercise){
        withContext(Dispatchers.IO) {
            service.upload(exercise)
            dao.insert(exercise)
        }
    }

    suspend fun getById(exerciseId: String): Exercise {
        return withContext(Dispatchers.IO) {
            dao.getById(exerciseId)
        }
    }

    fun getByIdFlow(exerciseId: String): Flow<Exercise?> {
        return dao.getByIdFlow(exerciseId)
    }

    suspend fun delete(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            service.delete(exercise)
            dao.delete(exercise)
        }
    }

    suspend fun update(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            val exercisetoUpdate =
            service.upload(exercise)
            dao.update(exercise)
        }
    }

    fun getAllExerciseNamesFlow(): Flow<List<String>> {
        return dao.getAllExerciseNamesFlow()
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return withContext(Dispatchers.IO) {
            dao.getExerciseName(exerciseId)
        }
    }

    suspend fun updateExerciseUsingById(exerciseId: String, isUsed: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateExerciseUsingById(exerciseId, isUsed)
        }
    }

    suspend fun getAll(): List<Exercise> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }

    suspend fun getVideoPath(exerciseId: String): String? {
        return withContext(Dispatchers.IO) {
            dao.getVideoPath(exerciseId)
        }
    }
}