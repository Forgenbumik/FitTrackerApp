package com.example.fittrackerapp.classes

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.entities.WorkoutDetail

data class WorkoutWithExercises(
    @Embedded val workout: Workout,

    @Relation(
        parentColumn = "id",          // Связывающий столбец в Workout
        entityColumn = "workout_id", // Связывающий столбец в WorkoutDetail
        associateBy = Junction(
            value = WorkoutDetail::class,
            parentColumn = "workout_id",
            entityColumn = "exercise_id"
        )
    )
    val exercises: List<Exercise>
)