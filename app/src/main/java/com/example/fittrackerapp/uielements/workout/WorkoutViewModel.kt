package com.example.fittrackerapp.uielements.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    val workoutId: Long,
    val workoutName: String,
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository
) : ViewModel() {

    private val _exercisesList = MutableStateFlow<List<WorkoutDetail>>(emptyList())
    val exercisesList: StateFlow<List<WorkoutDetail>> = _exercisesList

    init {
        if (workoutId != -1L) {
            loadWorkoutDetails(workoutId)
        }
    }

    fun loadWorkoutDetails(workoutId: Long) {
        viewModelScope.launch {
            _exercisesList.value = workoutDetailRepository.getByWorkoutId(workoutId)
                .sortedBy { wd -> wd.position }
        }
    }

    fun FormatTime(secs: Int): String {
        val minutes = secs / 60
        val seconds = secs % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}

class WorkoutViewModelFactory(
    private val workoutId: Long,
    private val workoutName: String,
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> {
                WorkoutViewModel(workoutId, workoutName, workoutRepository, workoutDetailRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}