package com.example.fittrackerapp.uielements.addingtousedworkouts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddingToUsedWorkoutsViewModel(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    var _exerciseNames: List<String> = emptyList()

    private val _isCreatingExercise = MutableStateFlow(false)
    val isCreatingExercise: StateFlow<Boolean> = _isCreatingExercise

    private val _exerciseName = MutableStateFlow("")
    private val exerciseName: StateFlow<String> = _exerciseName

    init {
        viewModelScope.launch {
            exerciseRepository.getAllExerciseNamesFlow().collect {
                _exerciseNames = it
                generateName()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addExercise(): Boolean {
        if (_exerciseNames.none { it.equals(exerciseName.value, ignoreCase = true) }) {
            viewModelScope.launch {
                exerciseRepository.insert(exerciseName.value)
            }
            return true
        }
        return false
    }

    fun setIsCreatingExercise(isCreatingExercise: Boolean) {
        _isCreatingExercise.value = isCreatingExercise
    }

    init {
        viewModelScope.launch {
            generateName()
        }
    }

    fun setExerciseName(name: String) {
        _exerciseName.value = name
    }

    fun getExerciseName(): String {
        return exerciseName.value
    }

    fun generateName() {
        _exerciseName.value = "Упражнение ${_exerciseNames.size + 1}"
    }
}

class AddingToUsedWorkoutsModelFactory(
    private val exerciseRepository: ExerciseRepository,
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddingToUsedWorkoutsViewModel::class.java) -> {
                AddingToUsedWorkoutsViewModel(exerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}