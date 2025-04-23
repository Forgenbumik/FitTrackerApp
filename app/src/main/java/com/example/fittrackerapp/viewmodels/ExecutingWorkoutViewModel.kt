package com.example.fittrackerapp.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class ExecutingWorkoutViewModel (
    private val workoutId: Long,
    exerciseId: Long,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val setsRepository: SetRepository,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
): ViewModel() {

    private val completedWorkout = CompletedWorkout(0, 0, "", LocalDateTime.now(), workoutId)

    var completedWorkoutId = 0L

    private lateinit var currentExercise: CompletedExercise

    private var currentExerciseId = exerciseId

    private val _currentExerciseName = MutableStateFlow("")
    val currentExerciseName: StateFlow<String> = _currentExerciseName

    private val _nextExercise = MutableStateFlow<WorkoutDetail>(WorkoutDetail(0, 0, 0, 0, "", 0, 0, 0, false))
    val nextExercise: StateFlow<WorkoutDetail> = _nextExercise

    private val _currentSet: MutableStateFlow<Set> = MutableStateFlow(Set(0, completedExerciseId = 0, duration = 0, reps = 0, weight = 0.0, restDuration = 0, setNumber = 0))
    val currentSet: StateFlow<Set> = _currentSet

    private val _setList = MutableStateFlow<List<Set>>(emptyList())
    val setList: StateFlow<List<Set>> = _setList

    private val _workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val workoutCondition: StateFlow<WorkoutCondition> = _workoutCondition

    private val _lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val lastCondition: StateFlow<WorkoutCondition> = _lastCondition

    private var workoutSeconds = 0

    private val _stringWorkoutTime = MutableStateFlow("00:00")
    val stringWorkoutTime: StateFlow<String> = _stringWorkoutTime

    private var exerciseSeconds = 0

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private var setSeconds = 0

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private var restSeconds = 0

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    private val _isChangingSet = MutableStateFlow(false)
    val isChangingSet: StateFlow<Boolean> = _isChangingSet

    private val _changingSet: MutableStateFlow<Set?> = MutableStateFlow(null)
    val changingSet: StateFlow<Set?> = _changingSet

    init {
        viewModelScope.launch {
            runWorkoutTimer()
        }
        viewModelScope.launch {
            runWorkout()
        }
    }

    fun setIsChangingSet(isChangingSet: Boolean) {
        _isChangingSet.value = isChangingSet
    }

    private suspend fun runWorkout() {
        completedWorkoutId = completedWorkoutRepository.insert(completedWorkout)
        var details = workoutDetailRepository.getByWorkoutId(workoutId).toMutableList()
        val firstExercise = details.first { it.exerciseId == currentExerciseId }
        details.remove(firstExercise)
        details.add(0, firstExercise)
        while (workoutCondition.value != WorkoutCondition.END) {
            for (i in 0..(details.size-1)) {
                if (i+1 != details.size) {
                    _nextExercise.value = details[i+1]
                }
                runExercise(details[i])
            }
        }
        lastWorkoutRepository.insertLastWorkout(completedWorkout)
    fun updateSet(set: Set, reps: Int, weight: Double) {
        set.reps = reps
        set.weight = weight
        viewModelScope.launch {
            setsRepository.update(set)
        }

    }

    private suspend fun runWorkoutTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.END -> {
                    completedWorkout.duration = workoutSeconds
                    completedWorkoutRepository.update(completedWorkout)
                    break
                }
                else -> workoutStopwatch()
            }
        }
    }

    suspend fun workoutStopwatch() {
        while (workoutCondition.value != WorkoutCondition.PAUSE
            && workoutCondition.value != WorkoutCondition.END) {
            workoutSeconds++
            _stringWorkoutTime.value = formatTime(workoutSeconds)
            delay(1000)
        }
    }

    fun setCondition(condition: WorkoutCondition) {
        if (_workoutCondition.value != condition) {
            _lastCondition.value = workoutCondition.value
            _workoutCondition.value = condition
        }
    }

    private suspend fun runExercise(detail: WorkoutDetail) {

        viewModelScope.launch {
            workoutCondition.collectLatest { condition ->
                when (condition) {
                    WorkoutCondition.SET -> exerciseStopwatch()
                    WorkoutCondition.PAUSE -> waitForResume()
                    WorkoutCondition.END -> return@collectLatest
                    else -> Unit
                }
                currentExercise.restDuration = restSeconds
            }
        }
        currentExercise = CompletedExercise(0, detail.exerciseId, 0, "", LocalDateTime.now(), completedWorkoutId, 0)
        currentExerciseId = completedExerciseRepository.insert(currentExercise)

        _currentExerciseName.value = detail.exerciseName

        for (i in 1..detail.setsNumber) {
            if (workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && workoutCondition.value != WorkoutCondition.END) {
                _currentSet.value.completedExerciseId = currentExerciseId
                _currentSet.value.reps = detail.reps
                _currentSet.value.setNumber = i
                val setId = setsRepository.insert(_currentSet.value)
                _setList.value += _currentSet.value
                runSetTimer()
                runRestTimer(setId, detail.restDuration)
                restSeconds = 0
            }
        }
        _setList.value = emptyList()
        runRestTimer(0,0)
        currentExercise.restDuration = restSeconds
        completedExerciseRepository.update(currentExercise)
    }

    private suspend fun runExerciseTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.END -> break
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.REST_AFTER_EXERCISE -> restAfterExerciseStopwatch()
                else -> exerciseStopwatch()
            }
        }
    }

    private suspend fun restAfterExerciseStopwatch() {
        while (workoutCondition.value == WorkoutCondition.REST_AFTER_EXERCISE) {
            restSeconds++
            _stringRestTime.value = formatTime(restSeconds)
            delay(1000)
        }
    }

    private suspend fun exerciseStopwatch() {
        while (workoutCondition.value == WorkoutCondition.SET
            && workoutCondition.value == WorkoutCondition.REST) {
            exerciseSeconds++
            _stringExerciseTime.value = formatTime(exerciseSeconds)
            delay(1000)
        }
    }

    private suspend fun runSetTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.SET -> setStopwatch()
                else -> break
            }
        }
    }

    private suspend fun waitForResume() {
        while (workoutCondition.value == WorkoutCondition.PAUSE
            && workoutCondition.value != WorkoutCondition.END) {
            delay(500) // Ждём, пока не изменится состояние
        }
    }

    private suspend fun setStopwatch() {
        while (workoutCondition.value == WorkoutCondition.SET) {
            setSeconds++
            _stringSetTime.value = formatTime(setSeconds)
            delay(1000)
        }
        _currentSet.value.duration = setSeconds
        setsRepository.update(_currentSet.value)
        _currentSet.value = Set(0, currentExerciseId, 0, 0, 0.0, 0, 0)
        setSeconds = 0
    }

    private suspend fun runRestTimer(setId: Long, restDuration: Int) {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.REST -> restStopwatch(restDuration)
                WorkoutCondition.PAUSE -> waitForResume()
                else -> break
            }
        }
        if (setId != 0L) {
            setsRepository.updateRestDuration(setId, restSeconds)
        }
    }



    suspend fun restStopwatch(duration: Int) {
        if (duration > 0) {
            while (workoutCondition.value == WorkoutCondition.REST
                && restSeconds < duration) {
                restSeconds++
                _stringRestTime.value = formatTime(restSeconds)
                delay(1000)
            }
        }
        else {
            while (workoutCondition.value == WorkoutCondition.REST || workoutCondition.value == WorkoutCondition.REST_AFTER_EXERCISE) {
                restSeconds++
                _stringRestTime.value = formatTime(restSeconds)
                delay(1000)
            }
        }
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun setChangingSet(set: Set?) {
        _changingSet.value = set;
    }
}

class ExecutingWorkoutViewModelFactory(
    private val workoutId: Long,
    private val exerciseId: Long,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val setsRepository: SetRepository,
    private val completedWorkoutRepository: CompletedWorkoutRepository,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ExecutingWorkoutViewModel::class.java) -> {
                ExecutingWorkoutViewModel(workoutId, exerciseId, workoutDetailRepository,
                    setsRepository, completedWorkoutRepository,
                    completedExerciseRepository, lastWorkoutRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}