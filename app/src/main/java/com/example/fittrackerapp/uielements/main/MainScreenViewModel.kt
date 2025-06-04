package com.example.fittrackerapp.uielements.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.ActiveWorkoutPrefs
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.dataStore
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.uielements.executingexercise.ExerciseRecordingCommunicator
import com.example.fittrackerapp.uielements.executingworkout.WorkoutRecordingCommunicator
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

    private val _activeWorkoutId = MutableStateFlow<String?>(null)
    val activeWorkoutId: StateFlow<String?> = _activeWorkoutId

    private val _workoutSeconds = WorkoutRecordingCommunicator.workoutSeconds
    val workoutSeconds: StateFlow<Int> = _workoutSeconds

    private val _exerciseSeconds = ExerciseRecordingCommunicator.exerciseSeconds
    val exerciseSeconds: StateFlow<Int> = _exerciseSeconds

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
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
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

    fun loadActiveWorkout(context: Context) {
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                _activeWorkoutId.value = prefs[ActiveWorkoutPrefs.ACTIVE_WORKOUT_ID]
            }
        }
    }
}