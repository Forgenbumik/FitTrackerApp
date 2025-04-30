package com.example.fittrackerapp.uielements.completedexercise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class CompletedExerciseViewModel(
    private val completedExerciseId: Long,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val setRepository: SetRepository
): ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _completedExercise = MutableStateFlow(CompletedExercise(0,0,0,"", LocalDateTime.now(), 0, 0, 0, 0))
    @RequiresApi(Build.VERSION_CODES.O)
    val completedExercise: StateFlow<CompletedExercise> = _completedExercise

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _changingSet: MutableStateFlow<Set> = MutableStateFlow(Set(0,0,0,0,0.0,0,0))
    val changingSet: StateFlow<Set> = _changingSet

    init {
        viewModelScope.launch {
            completedExerciseRepository.getById(completedExerciseId)
                .collect { newCompletedExercise ->
                    _completedExercise.value = newCompletedExercise
                }
            setRepository.getByCompletedExerciseId(completedExerciseId)
                .collect { newSets ->
                    _setList.clear()
                    _setList.addAll(newSets)
                }
        }
    }

    fun setChangingSet(set: Set) {
        _changingSet.value = set
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

class CompletedExerciseViewModelFactory(
    private val completedExerciseId: Long,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val setRepository: SetRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CompletedExerciseViewModel::class.java) -> {
                CompletedExerciseViewModel(completedExerciseId, completedExerciseRepository, setRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}