package com.example.fittrackerapp.uielements.executingexercise

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.set.SetRepository
import com.example.fittrackerapp.service.ServiceCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ExecutingExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
): ViewModel() {

    val _completedExerciseId = ExerciseRecordingCommunicator.completedExerciseId

    val exerciseId: StateFlow<String> = ExerciseRecordingCommunicator.exerciseId

    val isSaveCompleted: StateFlow<Boolean> = ExerciseRecordingCommunicator.isSaveCompleted

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    val changingSet: StateFlow<Set?> = ExerciseRecordingCommunicator.changingSet
    val workoutCondition: StateFlow<WorkoutCondition> = ExerciseRecordingCommunicator.workoutCondition
    val lastCondition: StateFlow<WorkoutCondition> = ExerciseRecordingCommunicator.lastCondition

    init {
        viewModelScope.launch {
            _completedExerciseId
                .filterNotNull()
                .filter { it != "" }
                .flatMapLatest {
                    setRepository.getByCompletedExerciseIdFlow(it)
                }
                .collect { newSets ->
                    _setList.clear()
                    _setList.addAll(newSets)
                }
        }
        viewModelScope.launch {
            ExerciseRecordingCommunicator.exerciseSeconds.collectLatest {
                updateExerciseTime(it)
            }
        }
        viewModelScope.launch {
            ExerciseRecordingCommunicator.setSeconds.collectLatest {
                updateSetTime(it)
            }
        }
        viewModelScope.launch {
            ExerciseRecordingCommunicator.restSeconds.collectLatest {
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
                WorkoutCondition.REST_AFTER_EXERCISE -> {}
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

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    suspend fun getExerciseVideoPath(exerciseId: String): String? {
        return exerciseRepository.getVideoPath(exerciseId)
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        run {
            sendCommand(ServiceCommand.UpdateSetCommand(set, reps, weight))
        }
    }
}