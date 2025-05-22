package com.example.fittrackerapp.uielements.executingexercise

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.service.ServiceCommand
import com.example.fittrackerapp.uielements.executingworkout.WorkoutRecordingCommunicator
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ExecutingExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
): ViewModel() {

    var completedExerciseId = 0L

    val exerciseId: StateFlow<Long> = ExerciseRecordingCommunicator.exerciseId

    val isSaveCompleted: StateFlow<Boolean> = WorkoutRecordingCommunicator.isSaveCompleted

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    val changingSet: StateFlow<Set?> = WorkoutRecordingCommunicator.changingSet
    val workoutCondition: StateFlow<WorkoutCondition> = WorkoutRecordingCommunicator.workoutCondition
    val lastCondition: StateFlow<WorkoutCondition> = WorkoutRecordingCommunicator.lastCondition

    init {
        if (completedExerciseId > 0) {
            viewModelScope.launch {
                setRepository.getByCompletedExerciseIdFlow(completedExerciseId)
                    .collect { newSets ->
                        _setList.clear()
                        _setList.addAll(newSets)
                    }
            }
            viewModelScope.launch {
                    setRepository.getByCompletedExerciseIdFlow(completedExerciseId).collect { newSets ->
                    _setList.clear()
                    _setList.addAll(newSets)
                }
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.exerciseSeconds.collectLatest {
                updateExerciseTime(it)
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.setSeconds.collectLatest {
                updateSetTime(it)
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.restSeconds.collectLatest {
                updateRestTime(it)
            }
        }
    }

    fun sendCommand(command: ServiceCommand) {
        viewModelScope.launch {
            ExerciseRecordingCommunicator.serviceCommands.send(command)
        }
    }

    fun updateExerciseTime(secs: Int) {
        _stringExerciseTime.value = formatTime(secs)
    }

    fun updateSetTime(secs: Int) {
        _stringSetTime.value = formatTime(secs)
    }

    fun updateRestTime(secs: Int) {
        _stringRestTime.value = formatTime(secs)
    }

    fun setCondition(condition: WorkoutCondition) {
        if (workoutCondition.value != condition) {
            when (condition) {
                WorkoutCondition.SET -> sendCommand(ServiceCommand.SetCommand)
                WorkoutCondition.REST -> sendCommand(ServiceCommand.RestCommand)
                WorkoutCondition.PAUSE -> sendCommand(ServiceCommand.PauseCommand)
                WorkoutCondition.REST_AFTER_EXERCISE -> sendCommand(ServiceCommand.RestAfterExerciseCommand)
                WorkoutCondition.END -> sendCommand(ServiceCommand.EndCommand)
            }
        }
    }

    fun setChangingSet(set: Set?) {
        run {
            sendCommand(ServiceCommand.SetChangingSetCommand(set))
        }
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    suspend fun getExerciseVideoPath(exerciseId: Long): String? {
        return exerciseRepository.getVideoPath(exerciseId)
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        run {
            sendCommand(ServiceCommand.UpdateSetCommand(set, reps, weight))
        }
    }
}