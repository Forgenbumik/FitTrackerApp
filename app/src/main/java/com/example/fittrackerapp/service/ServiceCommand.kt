package com.example.fittrackerapp.service

sealed class ServiceCommand {
    object Set: ServiceCommand()
    object Rest: ServiceCommand()
    object Pause : ServiceCommand()
    object RestAfterExercise : ServiceCommand()
    object End: ServiceCommand()
    data class UpdateSet(val set: com.example.fittrackerapp.entities.Set, val reps: Int, val weight: Double) : ServiceCommand()
    data class SetChangingSet(val set: com.example.fittrackerapp.entities.Set?) : ServiceCommand()
}