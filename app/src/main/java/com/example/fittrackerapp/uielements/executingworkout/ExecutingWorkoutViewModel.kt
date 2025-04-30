package com.example.fittrackerapp.uielements.executingworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.service.ServiceCommand
import com.example.fittrackerapp.service.WorkoutRecordingCommunicator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ExecutingWorkoutViewModel(
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val setsRepository: SetRepository,
): ViewModel() {

    var completedWorkoutId = 0L
    private var currentExerciseId = 0L

    private val _serviceCommands = MutableSharedFlow<ServiceCommand>()

    private val _currentExerciseName = MutableStateFlow("")
    val currentExerciseName: StateFlow<String> = _currentExerciseName

    private val _nextExercise = MutableStateFlow(WorkoutDetail(0, 0, 0, 0, "", 0, 0, 0, false))
    val nextExercise: StateFlow<WorkoutDetail> = _nextExercise

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _stringWorkoutTime = MutableStateFlow("00:00")
    val stringWorkoutTime: StateFlow<String> = _stringWorkoutTime

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    private val _isChangingSet = MutableStateFlow(false)
    val isChangingSet: StateFlow<Boolean> = _isChangingSet

    private val _changingSet: MutableStateFlow<Set?> = MutableStateFlow(null)
    val changingSet: StateFlow<Set?> = _changingSet

    private val _workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val workoutCondition: StateFlow<WorkoutCondition> = _workoutCondition

    private val _lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val lastCondition: StateFlow<WorkoutCondition> = _lastCondition

    init {
        viewModelScope.launch {
            setsRepository.getByCompletedExerciseId(currentExerciseId)
                .collect { newSets ->
                    _setList.clear()
                    _setList.addAll(newSets)
                }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.completedWorkoutId.collectLatest {
                completedWorkoutId = it
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.currentExercise.collectLatest {
                if (it != null) {
                    currentExerciseId = it.id
                }
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.currentExerciseName.collectLatest {
                _currentExerciseName.value = it
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.workoutCondition.collectLatest {
                _workoutCondition.value = it
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.lastCondition.collectLatest {
                _lastCondition.value = it
            }
        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.workoutSeconds.collectLatest {
                updateWorkoutTime(it)
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

        }
        viewModelScope.launch {
            WorkoutRecordingCommunicator.nextExercise.collectLatest {
                if (it != null) {
                    _nextExercise.value = it
                }
            }
        }
    }

    fun sendCommand(command: ServiceCommand) {
        viewModelScope.launch {
            _serviceCommands.emit(command)
        }
    }

    fun setIsChangingSet(isChangingSet: Boolean) {
        _isChangingSet.value = isChangingSet
    }

    fun updateWorkoutTime(secs: Int) {
        _stringWorkoutTime.value = formatTime(secs)
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
        if (_workoutCondition.value != condition) {
            sendCommand(ServiceCommand.Set)
        }
    }

    fun setChangingSet(set: Set?) {
        run {
            sendCommand(ServiceCommand.SetChangingSet(set))
        }
    }

    fun setNextExerciseById(workoutDetailId: Long) {
        viewModelScope.launch {
            val workoutDetail = workoutDetailRepository.getById(workoutDetailId)
            if (workoutDetail != null) {
                _nextExercise.value = workoutDetail
            }
        }
    }

    fun setCurrentExerciseId(id: Long) {
        currentExerciseId = id
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        run {
            sendCommand(ServiceCommand.UpdateSet(set, reps, weight))
        }
    }
}




class ExecutingWorkoutViewModelFactory(
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val setsRepository: SetRepository
): ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ExecutingWorkoutViewModel::class.java) -> {
                ExecutingWorkoutViewModel(workoutDetailRepository, setsRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}