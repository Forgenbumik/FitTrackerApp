package com.example.fittrackerapp.uielements.usedworkouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.FavouriteWorkoutRepository
import com.example.fittrackerapp.entities.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsedWorkoutsViewModel(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val workoutsRepository: WorkoutsAndExercisesRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<FavouriteWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<FavouriteWorkout>> = _favouriteWorkouts

    private val _workoutsList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val workoutsList: StateFlow<List<BaseWorkout>> = _workoutsList

    init {
        viewModelScope.launch {
            favouriteWorkoutRepository.getAllFlow().collect {
                _favouriteWorkouts.value = it
            }
        }
        viewModelScope.launch {
            workoutsAndExercisesRepository.getUsedExceptFavourites().collect {
                _workoutsList.value = it.sortedByDescending { bw -> bw.lastUsedDate }
            }
        }
    }

    fun addFavouriteWorkout(workout: BaseWorkout, position: Int): Boolean {
        if (_favouriteWorkouts.value.size >= 10 || !isUnique(workout)) {
            return false
        }
        viewModelScope.launch {
            favouriteWorkoutRepository.insert(workout, position)
        }
        return true
    }

    fun deleteFavouriteWorkout(workout: FavouriteWorkout) {
        viewModelScope.launch {
            favouriteWorkoutRepository.delete(workout)
        }
    }

    fun isUnique(workout: BaseWorkout): Boolean {
        return _favouriteWorkouts.value.none {
            var workoutTypeId = 0
            if (workout is Workout) {
                workoutTypeId = 1
            }
            else workoutTypeId = 2
            it.workoutId == workout.id
                    && it.typeId == workoutTypeId }
    }
}

class UsedWorkoutsViewModelFactory(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val workoutsRepository: WorkoutsAndExercisesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsedWorkoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsedWorkoutsViewModel(favouriteWorkoutRepository, workoutsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}