package com.example.fittrackerapp.repositories

import com.example.fittrackerapp.daoInterfaces.CategoryDao
import com.example.fittrackerapp.entities.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(private val dao: CategoryDao) {

    suspend fun getById(id: Int): Category? {
        return withContext(Dispatchers.IO) {
            dao.getById(id)
        }
    }

    suspend fun getAll(): Flow<List<Category>> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}