package com.example.fittrackerapp.entities.daoInterfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.Set

@Dao
interface FavouriteWorkoutDao {

    @Insert(entity = Set::class)
    suspend fun insert(favouriteWorkout: FavouriteWorkout)

    @Query("SELECT * FROM favourite_workouts")
    suspend fun getAll(): List<FavouriteWorkout>

    @Delete
    suspend fun delete(favouriteWorkout: FavouriteWorkout)

    @Query("SELECT * FROM favourite_workouts WHERE favouriteWorkoutPosition = :position")
    suspend fun getById(position: Int)
}