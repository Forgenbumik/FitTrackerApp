package com.example.fittrackerapp.uielements.executingworkout

import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.WorkoutDetail
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.service.ServiceCommand
import kotlinx.coroutines.channels.Channel

object WorkoutRecordingCommunicator {
    lateinit var serviceCommands: Channel<ServiceCommand>
        private set
    val completedWorkoutId = MutableStateFlow(0L)
    val currentExecExerciseId = MutableStateFlow<Long?>(0)
    val workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val workoutSeconds = MutableStateFlow(0)
    val exerciseSeconds = MutableStateFlow(0)
    val setSeconds = MutableStateFlow(0)
    val restSeconds = MutableStateFlow(0)
    val exerciseRestSeconds = MutableStateFlow(0)
    val changingSet = MutableStateFlow<Set?>(null)
    val nextExercise = MutableStateFlow<WorkoutDetail?>(null)
    val isSaveCompleted = MutableStateFlow(false)
    fun init() {
        serviceCommands = Channel(Channel.UNLIMITED)
    }
}