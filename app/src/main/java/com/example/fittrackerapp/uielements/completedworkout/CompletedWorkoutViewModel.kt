package com.example.fittrackerapp.uielements.completedworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class CompletedWorkoutViewModel(
    private val completedWorkoutId: Long,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _completedWorkout = MutableStateFlow(CompletedWorkout())
    @RequiresApi(Build.VERSION_CODES.O)
    val completedWorkout: StateFlow<CompletedWorkout> = _completedWorkout

    private val _completedExercises = MutableStateFlow<List<CompletedExercise>>(emptyList())
    val completedExercises = _completedExercises

    init {
        viewModelScope.launch {
            _completedWorkout.value = completedWorkoutRepository.getById(completedWorkoutId)
        }
        viewModelScope.launch {
            completedExerciseRepository.getByCompletedWorkoutIdFlow(completedWorkoutId).collect {
                _completedExercises.value = it
            }
        }
    }

    suspend fun getExerciseName(exerciseId: Long): String {
        return completedExerciseRepository.getExerciseName(exerciseId)
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    suspend fun getExerciseSetsNumber(exerciseId: Long): Int {
        return completedExerciseRepository.getSetsNumber(exerciseId)
    }

    suspend fun getExerciseTotalReps(exerciseId: Long): Int {
        return completedExerciseRepository.getTotalReps(exerciseId)
    }

    fun getWorkoutTotalExercises(): Int {
        return _completedExercises.value.size
    }

    suspend fun getWorkoutTotalSets(): Int {
        var totalSets = 0
        for (completedExercise in _completedExercises.value) {
            totalSets += completedExerciseRepository.getSetsNumber(completedExercise.id)
        }
        return totalSets
    }

    suspend fun getWorkoutTotalReps(): Int {
        var totalReps = 0
        for (completedExercise in _completedExercises.value) {
            totalReps += completedExerciseRepository.getTotalReps(completedExercise.id)
        }
        return totalReps
    }

    fun getIconPathByCompleted(baseCompletedWorkout: BaseCompletedWorkout): String? {
        var iconPath: String? = null
        if (baseCompletedWorkout is CompletedWorkout) {
            return iconPath
        } else if (baseCompletedWorkout is CompletedExercise) {
            viewModelScope.launch {
                iconPath =
                    completedExerciseRepository.getExerciseIconPath(baseCompletedWorkout.exerciseId)
            }.let {
                return iconPath
            }
        }
        return null
    }

    fun setWorkoutNotes(notes: String) {
        _completedWorkout.value = _completedWorkout.value.copy(notes = notes)
    }
}

class CompletedWorkoutViewModelFactory(
    private val completedWorkoutId: Long,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CompletedWorkoutViewModel::class.java) -> {
                CompletedWorkoutViewModel(completedWorkoutId, completedWorkoutRepository, completedExerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}