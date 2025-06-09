package com.example.fittrackerapp.uielements.executingworkout

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.set.SetRepository
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail
import com.example.fittrackerapp.service.ServiceCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class ExecutingWorkoutViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val setsRepository: SetRepository
): ViewModel() {

    val isSaveCompleted: StateFlow<Boolean> = WorkoutRecordingCommunicator.isSaveCompleted

    var completedWorkoutId = ""
    private val _currentExecExercise = WorkoutRecordingCommunicator.currentExecExercise

    val nextExercise: StateFlow<WorkoutDetail?> = WorkoutRecordingCommunicator.nextExercise

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    private val _stringExerciseRestTime = MutableStateFlow("")
    val stringExerciseRestTime: StateFlow<String> = _stringExerciseRestTime

    val changingSet: StateFlow<Set?> = WorkoutRecordingCommunicator.changingSet
    val workoutCondition: StateFlow<WorkoutCondition> = WorkoutRecordingCommunicator.workoutCondition
    val lastCondition: StateFlow<WorkoutCondition> = WorkoutRecordingCommunicator.lastCondition

    private val _currentExercise = MutableStateFlow(Exercise())
    val currentExercise: StateFlow<Exercise> = _currentExercise

    init {
        viewModelScope.launch {
            _currentExecExercise
                .filterNotNull()
                .filter { it.id != "" }
                .flatMapLatest {
                    setsRepository.getByCompletedExerciseIdFlow(it.id)
                }
                .collect { newSets ->
                    _setList.clear()
                    _setList.addAll(newSets)
                }
        }
        viewModelScope.launch {
            _currentExecExercise
                .filterNotNull()
                .collect {
                    _currentExercise.value = exerciseRepository.getById(it.exerciseId)
                }
        }

        viewModelScope.launch {
            WorkoutRecordingCommunicator.completedWorkoutId.collectLatest {
                completedWorkoutId = it
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
        viewModelScope.launch {
            WorkoutRecordingCommunicator.exerciseRestSeconds.collectLatest {
                updateExerciseRestTime(it)
            }
        }
    }

    fun sendCommand(command: ServiceCommand) {
        Log.d("ViewModel", "sendCommand: $command")
        viewModelScope.launch {
            WorkoutRecordingCommunicator.serviceCommands.send(command)
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

    fun updateExerciseRestTime(secs: Int) {
        _stringExerciseRestTime.value = formatTime(secs)
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

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        run {
            sendCommand(ServiceCommand.UpdateSetCommand(set, reps, weight))
        }
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return exerciseRepository.getExerciseName(exerciseId)
    }
}