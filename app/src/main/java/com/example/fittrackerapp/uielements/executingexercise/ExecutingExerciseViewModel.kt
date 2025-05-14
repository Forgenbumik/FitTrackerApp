package com.example.fittrackerapp.uielements.executingexercise

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ExecutingExerciseViewModel(
    private val exerciseId: Long,
    private val plannedSets: Int,
    private val plannedReps: Int,
    private val plannedRestDuration: Int,
    private val exerciseRepository: ExerciseRepository,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val setsRepository: SetRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
): ViewModel() {

    var exercise = Exercise()

    @RequiresApi(Build.VERSION_CODES.O)
    var completedExercise = CompletedExercise()

    var completedExerciseId = 0L

    val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    private var exerciseJob: Job? = null

    private val _currentSet: MutableStateFlow<Set> = MutableStateFlow(Set())
    val currentSet: StateFlow<Set> = _currentSet

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val workoutCondition: StateFlow<WorkoutCondition> = _workoutCondition

    private val _lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val lastCondition: StateFlow<WorkoutCondition> = _lastCondition

    private var exerciseSeconds = 0

    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    private var setSeconds = 0

    private val _stringSetTime = MutableStateFlow("00:00")
    val stringSetTime: StateFlow<String> = _stringSetTime

    private var restSeconds = 0

    private val _stringRestTime = MutableStateFlow("00:00")
    val stringRestTime: StateFlow<String> = _stringRestTime

    private val _changingSet: MutableStateFlow<Set?> = MutableStateFlow(null)
    val changingSet: StateFlow<Set?> = _changingSet

    init {
        viewModelScope.launch {
            exerciseRepository.getByIdFlow(exerciseId).collect {
                if (it != null) {
                    exercise = it
                }
            }
        }
        viewModelScope.launch {
            runExercise()
        }
        viewModelScope.launch {
            runExerciseTimer()
        }
    }

    fun setCondition(condition: WorkoutCondition) {
        if (_workoutCondition.value != condition) {
            _lastCondition.value = workoutCondition.value
            _workoutCondition.value = condition
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runExercise() {
        completedExercise = CompletedExercise(exerciseId = exerciseId)
        completedExerciseId = completedExerciseRepository.insert(completedExercise)
        completedExercise = completedExercise.copy(id = completedExerciseId)

        for (i in 1..plannedSets) {
            if (workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && workoutCondition.value != WorkoutCondition.END) {

                runSetTimer()
                _currentSet.value = Set(completedExerciseId = completedExerciseId, duration =  setSeconds, reps = plannedReps, setNumber =  i)
                val setId = setsRepository.insert(_currentSet.value)
                _currentSet.value = _currentSet.value.copy(id = setId)
                _setList.add(_currentSet.value)
                runRestTimer(setId, plannedRestDuration)
                _currentSet.value = _currentSet.value.copy(restDuration = restSeconds)
                restSeconds = 0
            }
        }
        _setList.clear()
        completedExercise = completedExercise.copy(duration = exerciseSeconds)
        completedExerciseRepository.update(completedExercise)
        insertLastWorkout()
        setCondition(WorkoutCondition.END)
    }

    private suspend fun runExerciseTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.SET -> exerciseStopwatch()
                WorkoutCondition.REST -> exerciseStopwatch()
                else -> break
            }
        }
    }

    private suspend fun exerciseStopwatch() {
        while (workoutCondition.value == WorkoutCondition.SET
            || workoutCondition.value == WorkoutCondition.REST) {

            delay(10) // Мини-пауза, чтобы убедиться, что состояние не изменилось
            if (workoutCondition.value != WorkoutCondition.SET
                && workoutCondition.value != WorkoutCondition.REST) break

            exerciseSeconds++
            _stringExerciseTime.value = formatTime(exerciseSeconds)
            delay(1000)
        }
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        val newSet = set.copy(reps = reps, weight = weight)
        _setList[set.setNumber - 1] = newSet
        viewModelScope.launch {
            setsRepository.update(newSet)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertLastWorkout() {
        if (_isSaveCompleted.value) return
        viewModelScope.launch {
            lastWorkoutRepository.insertLastWorkout(completedExercise)
            Log.d("LastWorkout", "Last workout inserted")
            _isSaveCompleted.value = true
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
        setSeconds = 0
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
            while (workoutCondition.value == WorkoutCondition.REST) {
                restSeconds++
                _stringRestTime.value = formatTime(restSeconds)
                delay(1000)
            }
        }
        if (restSeconds == duration) {
            setCondition(WorkoutCondition.SET)
        }
    }

    fun formatTime(secs: Int): String {
        val seconds = secs % 60
        val minutes = secs / 60 % 60
        val hours = secs / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun setChangingSet(set: Set?) {
        _changingSet.value = set
    }
}

class ExecutingExerciseViewModelFactory(
    private val exerciseId: Long,
    private val plannedSets: Int,
    private val plannedReps: Int,
    private val plannedRestDuration: Int,
    private val exerciseRepository: ExerciseRepository,
    private val completedExerciseRepository: CompletedExerciseRepository,
    private val setsRepository: SetRepository,
    private val lastWorkoutRepository: LastWorkoutRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExecutingExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExecutingExerciseViewModel(
                exerciseId, plannedSets, plannedReps,
                plannedRestDuration, exerciseRepository,
                completedExerciseRepository, setsRepository,
                lastWorkoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}