package com.example.fittrackerapp.abstractclasses

import java.time.LocalDateTime

abstract class BaseCompletedWorkout
{
    abstract val id: String
    abstract val duration: Int
    abstract val notes: String?
    abstract val beginTime: LocalDateTime
}