package com.example.fittrackerapp.entities.repositories

import com.example.fittrackerapp.entities.daoInterfaces.SetDao
import com.example.fittrackerapp.entities.Set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetRepository(private val dao: SetDao) {

    suspend fun insert(set: Set) {
        return withContext(Dispatchers.IO) {
            dao.insert(set)
        }
    }

    suspend fun getById(setId: Int): Set? {
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
}