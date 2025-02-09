package com.example.fittrackerapp.entities.repositories

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.daoInterfaces.FavouriteWorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavouriteWorkoutRepository(private val dao: FavouriteWorkoutDao) {

    suspend fun insert(favouriteWorkout: FavouriteWorkout) {
        withContext(Dispatchers.IO) {
            dao.insert(favouriteWorkout)
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

    suspend fun getById(position: Int) {
        return withContext(Dispatchers.IO) {
            dao.getById(position)
        }
    }
}