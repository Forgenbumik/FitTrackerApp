package com.example.fittrackerapp.abstractclasses

import java.time.LocalDateTime

abstract class BaseWorkout {

    abstract val id: String
    abstract val name: String
    abstract val isUsed: Boolean
    abstract val lastUsedDate: LocalDateTime
    abstract val isFavourite: Boolean
    abstract val isDeleted: Boolean
}