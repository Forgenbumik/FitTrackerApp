package com.example.fittrackerapp.uielements.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository,
    private val lastWorkoutRepository: LastWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<BaseWorkout>> = _favouriteWorkouts

    private val _lastWorkouts = MutableStateFlow<List<LastWorkout>>(emptyList())
    val lastWorkouts: StateFlow<List<LastWorkout>> = _lastWorkouts

    init {
        loadFavourites()
        loadLastWorkouts()
    }

    fun loadFavourites() {
        viewModelScope.launch {
            workoutsAndExercisesRepository.getFavouritesFlow().collect {
                _favouriteWorkouts.value = it
            }
        }
    }

    fun loadLastWorkouts() {
        viewModelScope.launch {
            _lastWorkouts.value = lastWorkoutRepository.getLastWorkouts()
        }
    }

    fun formatTime(secs: Int): String {
        val hours = secs / 3600
        val minutes = secs / 60
        val seconds = secs % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun getIconPathByCompleted(lastWorkout: LastWorkout): String? {
        var iconPath: String? = null
        if (lastWorkout.typeId == 1) {
            return iconPath
        }
        else {
            viewModelScope.launch {
                iconPath = completedExerciseRepository.getExerciseIconPath(lastWorkout.completedWorkoutId)
            }.let {
                return iconPath
            }
        }
    }
}

class MainScreenModelFactory(
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository,
    private val lastWorkoutRepository: LastWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainScreenViewModel(workoutsAndExercisesRepository, lastWorkoutRepository, completedExerciseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}