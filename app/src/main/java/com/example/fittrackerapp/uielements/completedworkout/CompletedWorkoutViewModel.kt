package com.example.fittrackerapp.uielements.completedworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CompletedWorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository
): ViewModel() {

    private val completedWorkoutId: String? get() = savedStateHandle["completedWorkoutId"]

    @RequiresApi(Build.VERSION_CODES.O)
    private val _completedWorkout = MutableStateFlow(CompletedWorkout())
    @RequiresApi(Build.VERSION_CODES.O)
    val completedWorkout: StateFlow<CompletedWorkout> = _completedWorkout

    private val _completedExercises = MutableStateFlow<List<CompletedExercise>>(emptyList())
    val completedExercises = _completedExercises

    init {
        if (completedWorkoutId != null && completedWorkoutId!! != "") {
            viewModelScope.launch {
                _completedWorkout.value = completedWorkoutRepository.getById(completedWorkoutId!!)
            }
            viewModelScope.launch {
                completedWorkoutId?.let {
                    completedExerciseRepository.getByCompletedWorkoutIdFlow(it).collect {
                        _completedExercises.value = it
                    }
                }
            }
        }
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return completedExerciseRepository.getExerciseName(exerciseId)
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

    suspend fun getExerciseSetsNumber(exerciseId: String): Int {
        return completedExerciseRepository.getSetsNumber(exerciseId)
    }

    suspend fun getExerciseTotalReps(exerciseId: String): Int {
        val totalReps = viewModelScope.async {
            // длительная операция
            completedExerciseRepository.getTotalReps(exerciseId)
        }
        if (totalReps.await() == null) {
            return 0
        }
        return totalReps.await()
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

    fun deleteCompeletedExercise(completedExercise: CompletedExercise) {
        viewModelScope.launch {
            completedExerciseRepository.delete(completedExercise)
        }
    }

    fun saveWorkoutNotes(notes: String?) {
        _completedWorkout.value = _completedWorkout.value.copy(notes = notes)
        viewModelScope.launch {
            completedWorkoutRepository.update(_completedWorkout.value)
        }
    }
}