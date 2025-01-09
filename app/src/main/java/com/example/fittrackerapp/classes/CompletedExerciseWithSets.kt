package com.example.fittrackerapp.classes

import androidx.room.Embedded
import androidx.room.Relation
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.Set

class CompletedExerciseWithSets(

    @Embedded
    val completedExercise: CompletedExercise,

    @Relation(
        parentColumn = "id",
        entityColumn = "completed_exercise_id"
    )
    val completedSets: List<Set>

)