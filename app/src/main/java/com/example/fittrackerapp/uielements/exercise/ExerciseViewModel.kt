package com.example.fittrackerapp.uielements.exercise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
): ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise

    private val _plannedSets = MutableStateFlow(0)
    val plannedSets: StateFlow<Int> = _plannedSets

    private val _plannedReps = MutableStateFlow(0)
    val plannedReps: StateFlow<Int> = _plannedReps

    private val _plannedRestDuration = MutableStateFlow(0)
    val plannedRestDuration: StateFlow<Int> = _plannedRestDuration

    val exerciseId: String? get() = savedStateHandle["exerciseId"]

    init {
        viewModelScope.launch {
            exerciseRepository.getByIdFlow(exerciseId!!).collect {
                _exercise.value = it
            }
        }
    }

    fun setPlannedSets(setsNum: Int) {
        _plannedSets.value = setsNum
    }

    fun setPlannedReps(repsNum: Int) {
        _plannedReps.value = repsNum
    }

    fun setPlannedRestDuration(duration: Int) {
        _plannedRestDuration.value = duration
    }


}