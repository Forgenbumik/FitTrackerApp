package com.example.fittrackerapp.entities.daoInterfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fittrackerapp.entities.Set

@Dao
interface SetDao {

    @Insert(entity = Set::class)
    suspend fun insert(set: Set)

    @Query("SELECT * FROM sets WHERE id = :setId")
    suspend fun getById(setId: Int): Set?

    @Delete
    suspend fun delete(set: Set)

    @Update
    suspend fun update(set: Set)
}