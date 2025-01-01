package com.example.fittrackerapp.daoInterfaces

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.fittrackerapp.entities.Set

interface SetDao {

    @Insert(entity = Set::class)
    suspend fun insertNewSet(set: Set)

    @Query("SELECT * FROM sets WHERE id = :setId")
    suspend fun getSetById(setId: Int): Set?

    @Delete
    suspend fun deleteSet(set: Set)

}