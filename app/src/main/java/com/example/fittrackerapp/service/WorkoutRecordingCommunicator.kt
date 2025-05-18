package com.example.fittrackerapp.service

import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.WorkoutDetail
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.fittrackerapp.entities.Set
import kotlinx.coroutines.flow.MutableSharedFlow

object WorkoutRecordingCommunicator {
    val serviceCommands = MutableSharedFlow<ServiceCommand>()
    val completedWorkoutId = MutableStateFlow(0L)
    val currentExercise = MutableStateFlow<CompletedExercise?>(null)
    val currentExerciseName = MutableStateFlow("")
    val workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val workoutSeconds = MutableStateFlow(0)
    val exerciseSeconds = MutableStateFlow(0)
    val setSeconds = MutableStateFlow(0)
    val restSeconds = MutableStateFlow(0)
    val changingSet = MutableStateFlow<Set?>(null)
    val nextExercise = MutableStateFlow<WorkoutDetail?>(null)

}