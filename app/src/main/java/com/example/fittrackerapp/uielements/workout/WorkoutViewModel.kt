package com.example.fittrackerapp.uielements.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val workoutId: String? get() = savedStateHandle["workoutId"]

    private val _exercisesList = MutableStateFlow<List<WorkoutDetail>>(emptyList())
    val exercisesList: StateFlow<List<WorkoutDetail>> = _exercisesList

    init {
        if (workoutId != "") {
            loadWorkoutDetails(workoutId!!)
        }
    }

    fun loadWorkoutDetails(workoutId: String) {
        viewModelScope.launch {
            _exercisesList.value = workoutDetailRepository.getByWorkoutId(workoutId)
                .sortedBy { wd -> wd.position }
        }
    }

    fun formatTime(secs: Int): String {
        val minutes = secs / 60
        val seconds = secs % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return exerciseRepository.getExerciseName(exerciseId)
    }
}