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
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "favourite_workouts",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Type::class,
            parentColumns = ["id"],
            childColumns = ["type_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ])
data class FavouriteWorkout(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val position: Int,
    @ColumnInfo(name="workout_id") val workoutId: Long,
    @ColumnInfo(name = "workout_name") val workoutName: String,
    @ColumnInfo(name = "type_id") val typeId: Int
)

@Dao
interface FavouriteWorkoutDao {

    @Insert(entity = FavouriteWorkout::class)
    suspend fun insert(favouriteWorkout: FavouriteWorkout)

    @Query("SELECT * FROM favourite_workouts")
    suspend fun getAll(): List<FavouriteWorkout>

    @Delete
    suspend fun delete(favouriteWorkout: FavouriteWorkout)

    @Query("SELECT * FROM favourite_workouts WHERE id = :id")
    suspend fun getById(id: Long): FavouriteWorkout

    @Update
    suspend fun update(favouriteWorkout: FavouriteWorkout)

    @Query("UPDATE favourite_workouts SET position = :order WHERE id = :id")
    suspend fun updateWorkoutOrder(id: Long, order: Int)
}

class FavouriteWorkoutRepository(private val dao: FavouriteWorkoutDao) {

    suspend fun insert(baseWorkout: BaseWorkout, position: Int) {
        val workout: FavouriteWorkout
        when (baseWorkout) {
            is Workout -> {
                //val favourites = getAll()
                //if (favourites)
                workout = FavouriteWorkout(0, position, baseWorkout.id, baseWorkout.name, 1)
                dao.insert(workout)
            }
            is Exercise -> {
                workout = FavouriteWorkout(0, position, baseWorkout.id, baseWorkout.name, 2)
                dao.insert(workout)
            }
        }
    }

    suspend fun getAll(): List<FavouriteWorkout> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }

    suspend fun delete(favouriteWorkout: FavouriteWorkout) {
        withContext(Dispatchers.IO) {
            dao.delete(favouriteWorkout)
        }
    }

    suspend fun getById(id: Long): FavouriteWorkout {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    suspend fun updateWorkoutOrder(id: Long, order: Int) {
        withContext(Dispatchers.IO) {
            dao.updateWorkoutOrder(id, order)
        }
    }
}