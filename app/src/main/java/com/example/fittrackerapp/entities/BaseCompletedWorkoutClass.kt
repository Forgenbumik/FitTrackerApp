package com.example.fittrackerapp.entities

sealed class BaseCompletedWorkoutClass
{
    abstract val duration: Int
    abstract val notes: String
    abstract val beginTime: String
    abstract val type: String
}