package com.example.fittrackerapp.service

import com.example.fittrackerapp.entities.Set

sealed class ServiceCommand {
    object SetCommand: ServiceCommand()
    object RestCommand: ServiceCommand()
    object PauseCommand : ServiceCommand()
    object RestAfterExerciseCommand : ServiceCommand()
    object EndCommand: ServiceCommand()
    data class UpdateSetCommand(val set: Set, val reps: Int, val weight: Double) : ServiceCommand()
    data class SetChangingSetCommand(val set: Set?) : ServiceCommand()
}