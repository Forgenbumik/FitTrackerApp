package com.example.fittrackerapp.classes

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedWorkout

data class CompletedWorkoutWithExercises(

    @Embedded
    val completedWorkout: CompletedWorkout,

    @Relation(
        parentColumn = "id",
        entityColumn = "completed_workout_id"
    )
    var completedExercises: List<CompletedExercise>
)