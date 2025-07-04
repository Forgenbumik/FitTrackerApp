package com.example.fittrackerapp.uielements.completedexercise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.set.SetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CompletedExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val setRepository: SetRepository
): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _completedExercise = MutableStateFlow(CompletedExercise())
    @RequiresApi(Build.VERSION_CODES.O)
    val completedExercise: StateFlow<CompletedExercise> = _completedExercise

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _changingSet: MutableStateFlow<Set?> = MutableStateFlow(Set())
    val changingSet: StateFlow<Set?> = _changingSet

    val completedExerciseId: String? get() = savedStateHandle["completedExerciseId"]

    init {
        viewModelScope.launch {
            completedExerciseId?.let { loadCompletedExercise(it) }
        }
        viewModelScope.launch {
            completedExerciseId?.let {
                setRepository.getByCompletedExerciseIdFlow(it).collect { newList ->
                    _setList.clear()
                    _setList.addAll(newList)
                }
            }
        }
    }

    suspend fun loadCompletedExercise(completedExerciseId: String) {
        _completedExercise.value = completedExerciseRepository.getById(completedExerciseId)
    }

    fun setChangingSet(set: Set?) {
        _changingSet.value = set
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        val newSet = set.copy(reps = reps, weight = weight)
        _setList[set.setNumber - 1] = newSet
        viewModelScope.launch {
            setRepository.update(newSet)
        }
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

    fun deleteSet(set: Set) {
        viewModelScope.launch {
            setRepository.delete(set)
        }
    }

    fun saveWorkoutNotes(notes: String?) {
        viewModelScope.launch {
            _completedExercise.value = _completedExercise.value.copy(notes = notes)
            completedExerciseRepository.update(_completedExercise.value)
        }
    }
}