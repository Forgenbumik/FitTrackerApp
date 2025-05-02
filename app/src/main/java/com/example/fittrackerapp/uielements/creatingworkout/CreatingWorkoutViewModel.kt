package com.example.fittrackerapp.uielements.creatingworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository

import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CreatingWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
): ViewModel() {
    private var workoutName = ""

    private var generatedName = ""

    private val _exercisesList = MutableStateFlow<List<Exercise>>(emptyList())
    val exercisesList = _exercisesList

    init {
        viewModelScope.launch {
            generateName()
        }

    }

    fun setWorkoutName(name: String) {
        workoutName = name
    }

    fun getWorkoutName(): String {
        return workoutName
    }

    fun getGeneratedName(): String {
        return generatedName
    }

    suspend fun generateName() {
        generatedName = "Сценарий ${workoutRepository.getWorkoutsNames().size + 1}"
    }

    suspend fun getExercises() {
        exerciseRepository.getAllExceptNotUsed()
    }
}

class CreatingWorkoutViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CreatingWorkoutViewModel::class.java) -> {
                CreatingWorkoutViewModel(workoutRepository, workoutDetailRepository, exerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}