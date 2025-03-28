package com.example.fittrackerapp.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.FavouriteWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AllWorkoutsViewModel(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val workoutsRepository: WorkoutsAndExercisesRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<FavouriteWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<FavouriteWorkout>> = _favouriteWorkouts

    private val _workoutsList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val workoutsList: StateFlow<List<BaseWorkout>> = _workoutsList

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _workoutsList.value = workoutsRepository.getAll().sortedByDescending { bw -> bw.lastUsedDate }
            _favouriteWorkouts.value = favouriteWorkoutRepository.getAll()
        }
    }

    fun addFavouriteWorkout(workout: BaseWorkout, position: Int): Boolean {
        if (_favouriteWorkouts.value.size >= 10) {
            return false
        }
        viewModelScope.launch {
            favouriteWorkoutRepository.insert(workout, position)
            _favouriteWorkouts.value = favouriteWorkoutRepository.getAll()
        }
        return true
    }

    fun deleteFavouriteWorkout(workout: FavouriteWorkout) {
        viewModelScope.launch {
            favouriteWorkoutRepository.delete(workout)
            _favouriteWorkouts.value = favouriteWorkoutRepository.getAll()
        }
    }
}

class AllWorkoutsModelFactory(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val workoutsRepository: WorkoutsAndExercisesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllWorkoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllWorkoutsViewModel(favouriteWorkoutRepository, workoutsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}