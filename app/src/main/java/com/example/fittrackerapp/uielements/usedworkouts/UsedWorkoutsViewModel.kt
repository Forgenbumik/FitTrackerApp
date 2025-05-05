package com.example.fittrackerapp.uielements.usedworkouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsedWorkoutsViewModel(
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<BaseWorkout>> = _favouriteWorkouts

    private val _workoutsList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val workoutsList: StateFlow<List<BaseWorkout>> = _workoutsList

    private val _selectedWorkout = MutableStateFlow<BaseWorkout?>(null)
    val selectedWorkout: StateFlow<BaseWorkout?> = _selectedWorkout

    init {
        viewModelScope.launch {
            workoutsAndExercisesRepository.getFavouritesFlow().collect {
                _favouriteWorkouts.value = it
            }
        }
        viewModelScope.launch {
            workoutsAndExercisesRepository.getUsedExceptFavouritesFlow().collect {
                _workoutsList.value = it.sortedByDescending { bw -> bw.lastUsedDate }
            }
        }
    }

    fun addFavouriteWorkout(workout: BaseWorkout): Boolean {
        if (_favouriteWorkouts.value.size >= 10 && isUnique(workout)) {
            workout.isFavourite = true
            return false
        }
        viewModelScope.launch {
            workoutsAndExercisesRepository.addFavourite(workout)
        }
        return true
    }

    fun removeFavouriteWorkout(workout: BaseWorkout) {
        workout.isFavourite = false
        viewModelScope.launch {
            workoutsAndExercisesRepository.deleteFavourite(workout)
        }
    }

    fun isUnique(workout: BaseWorkout): Boolean {
        return _favouriteWorkouts.value.none {
            it.id == workout.id
                    && it.javaClass.simpleName == workout.javaClass.simpleName }
    }

    fun removeWorkoutFromUsed(workout: BaseWorkout) {
        viewModelScope.launch {
            workoutsAndExercisesRepository.removeFromUsed(workout)
        }
    }

    fun deleteWorkout(workout: BaseWorkout) {
        viewModelScope.launch {
            workoutsAndExercisesRepository.delete(workout)
        }
    }

    fun setSelectedWorkout(baseWorkout: BaseWorkout) {
        _selectedWorkout.value = baseWorkout
    }
}

class UsedWorkoutsViewModelFactory(
    private val workoutsRepository: WorkoutsAndExercisesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsedWorkoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsedWorkoutsViewModel(workoutsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}