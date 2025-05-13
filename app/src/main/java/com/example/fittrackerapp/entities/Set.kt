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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = CompletedExercise::class,
            parentColumns = ["id"],
            childColumns = ["completed_exercise_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Set(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "completed_exercise_id") val completedExerciseId: Long = 0,
    @ColumnInfo(name = "duration") val duration: Int = 0,
    @ColumnInfo(name = "reps") val reps: Int = 0,
    @ColumnInfo(name = "weight") val weight: Double = 0.0,
    @ColumnInfo(name = "rest_duration") val restDuration: Int = 0,
    @ColumnInfo(name = "set_number") val setNumber: Int = 0
)

@Dao
interface SetDao {

    @Insert(entity = Set::class)
    suspend fun insert(set: Set): Long

    @Query("SELECT * FROM sets WHERE id = :setId")
    suspend fun getById(setId: Long): Set?

    @Delete
    suspend fun delete(set: Set)

    @Update
    suspend fun update(set: Set)

    @Query("UPDATE sets SET rest_duration = :restDuration WHERE id = :id")
    suspend fun updateRestDuration(id: Long, restDuration: Int)

    @Query("SELECT * FROM sets WHERE completed_exercise_id = :completedExerciseId")
    suspend fun getByCompletedExerciseId(completedExerciseId: Long): List<Set>

    @Query("SELECT * FROM sets WHERE completed_exercise_id = :completedExerciseId")
    fun getByCompletedExerciseIdFlow(completedExerciseId: Long): Flow<List<Set>>
}

class SetRepository(private val dao: SetDao) {

    suspend fun insert(set: Set): Long {
        return withContext(Dispatchers.IO) {
            dao.insert(set)
        }
    }

    suspend fun getById(setId: Long): Set? {
        return withContext(Dispatchers.IO) {
            dao.getById(setId)
        }
    }

    suspend fun delete(set: Set) {
        withContext(Dispatchers.IO) {
            dao.delete(set)
        }
    }

    suspend fun update(set: Set) {
        withContext(Dispatchers.IO) {
            dao.update(set)
        }
    }

    suspend fun updateRestDuration(id: Long, restDuration: Int) {
        withContext(Dispatchers.IO) {
            dao.updateRestDuration(id, restDuration)
        }
    }

    suspend fun getByCompletedExerciseId(completedExerciseId: Long): List<Set> {
        return withContext(Dispatchers.IO) {
            dao.getByCompletedExerciseId(completedExerciseId)
        }
    }

    fun getByCompletedExerciseIdFlow(completedExerciseId: Long): Flow<List<Set>> {
        return dao.getByCompletedExerciseIdFlow(completedExerciseId)
    }
}