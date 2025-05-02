package com.example.fittrackerapp.uielements.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.FavouriteWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<FavouriteWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<FavouriteWorkout>> = _favouriteWorkouts

    private val _lastWorkouts = MutableStateFlow<List<LastWorkout>>(emptyList())
    val lastWorkouts: StateFlow<List<LastWorkout>> = _lastWorkouts

    init {
        loadFavourites()
        loadLastWorkouts()
    }

    fun loadFavourites() {
        viewModelScope.launch {
            favouriteWorkoutRepository.getAllFlow().collect {
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

    fun moveItem(from: Int, to: Int) { //для перетаскивания элементов списка
        val list = _favouriteWorkouts.value.toMutableList()
        val movedItem = list.removeAt(from)
        list.add(to, movedItem)

        _favouriteWorkouts.value = list

        saveNewOrder(list)
    }

    private fun saveNewOrder(list: List<FavouriteWorkout>) { //для перетаскивания элементов списка
        viewModelScope.launch {
            list.forEachIndexed { index, workout ->
                favouriteWorkoutRepository.updateWorkoutOrder(workout.id, index)
            }
        }
    }
}

class MainScreenModelFactory(
    private val favouriteWorkoutRepository: FavouriteWorkoutRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainScreenViewModel(favouriteWorkoutRepository, lastWorkoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}