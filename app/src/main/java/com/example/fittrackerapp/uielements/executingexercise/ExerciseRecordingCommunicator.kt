package com.example.fittrackerapp.uielements.executingexercise

import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.service.ServiceCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

object ExerciseRecordingCommunicator {
    lateinit var serviceCommands: Channel<ServiceCommand>
        private set
    val exerciseId = MutableStateFlow("")
    val completedExerciseId = MutableStateFlow("")
    val workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val exerciseSeconds = MutableStateFlow(0)
    val setSeconds = MutableStateFlow(0)
    val restSeconds = MutableStateFlow(0)
    val changingSet = MutableStateFlow<Set?>(null)
    val isSaveCompleted = MutableStateFlow(false)
    fun init() {
        serviceCommands = Channel(Channel.UNLIMITED)
    }
}