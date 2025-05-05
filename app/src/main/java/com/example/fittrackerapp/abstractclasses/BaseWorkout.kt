package com.example.fittrackerapp.abstractclasses

import java.time.LocalDateTime

abstract class BaseWorkout {
    abstract val id: Long
    abstract val name: String
    abstract var isUsed: Boolean
    abstract val lastUsedDate: LocalDateTime
    abstract var isFavourite: Boolean
    abstract var isDeleted: Boolean
}