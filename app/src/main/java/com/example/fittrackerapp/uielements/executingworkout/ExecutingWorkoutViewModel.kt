package com.example.fittrackerapp.uielements.executingworkout

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

    private val completedWorkout = CompletedWorkout(0, 0, "", LocalDateTime.now(), workoutId, 0)

    var completedWorkoutId = 0L

    val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    private var exerciseJob: Job? = null

    private lateinit var currentExercise: CompletedExercise

    private var currentExerciseId = exerciseId

    private val _currentExerciseName = MutableStateFlow("")
    val currentExerciseName: StateFlow<String> = _currentExerciseName

    private val _nextExercise = MutableStateFlow(WorkoutDetail(0, 0, 0, 0, "", 0, 0, 0, false))
    val nextExercise: StateFlow<WorkoutDetail> = _nextExercise

    private val _currentSet: MutableStateFlow<Set> = MutableStateFlow(Set(0, completedExerciseId = 0, duration = 0, reps = 0, weight = 0.0, restDuration = 0, setNumber = 0))
    val currentSet: StateFlow<Set> = _currentSet

    private var _setList = mutableStateListOf<Set>()
    val setList: SnapshotStateList<Set> get() = _setList

    private val _workoutCondition = MutableStateFlow(WorkoutCondition.SET)
    val workoutCondition: StateFlow<WorkoutCondition> = _workoutCondition

    private val _lastCondition = MutableStateFlow(WorkoutCondition.PAUSE)
    val lastCondition: StateFlow<WorkoutCondition> = _lastCondition

    private var workoutSeconds = 0

    private val _stringWorkoutTime = MutableStateFlow("00:00")
    val stringWorkoutTime: StateFlow<String> = _stringWorkoutTime

    private var exerciseSeconds = 0

    private var exerciseRestSeconds = 0

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
        completedWorkout.id = completedWorkoutId
        var details = workoutDetailRepository.getByWorkoutId(workoutId).toMutableList()
        val firstExercise = details.first { it.exerciseId == currentExerciseId }
        details.remove(firstExercise)
        details.add(0, firstExercise)
        for (i in 0..(details.size-1)) {
            if (_workoutCondition.value != WorkoutCondition.END) {
                if (i+1 != details.size) {
                    _nextExercise.value = details[i+1]
                }
                runExercise(details[i])
                currentExercise.duration = exerciseSeconds
                exerciseSeconds = 0
                if (_workoutCondition.value != WorkoutCondition.END) {
                    setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                }
                runRestAfterExerciseTimer()

                currentExercise.restDuration = exerciseRestSeconds
                exerciseRestSeconds = 0
                completedExerciseRepository.update(currentExercise)
                completedWorkout.exercisesNumber++
            }
        }

        completedWorkout.duration = workoutSeconds
        completedWorkoutRepository.update(completedWorkout)
        delay(1000)
        insertLastWorkout()
        setCondition(WorkoutCondition.END)
    }

     fun insertLastWorkout() {
         if (_isSaveCompleted.value) return
         viewModelScope.launch {
             lastWorkoutRepository.insertLastWorkout(completedWorkout)
             Log.d("LastWorkout", "Last workout inserted")
             _isSaveCompleted.value = true
         }

    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        val newSet = set.copy(reps = reps, weight = weight)
        _setList[set.setNumber - 1] = newSet
        viewModelScope.launch {
            setsRepository.update(newSet)
        }

    }

    private suspend fun runWorkoutTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.END -> break
                else -> workoutStopwatch()
            }
        }
    }

    suspend fun workoutStopwatch() {
        while (workoutCondition.value != WorkoutCondition.PAUSE
            && workoutCondition.value != WorkoutCondition.END) {
            workoutSeconds++
            completedWorkout.duration = workoutSeconds
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

        exerciseJob?.cancel()
        exerciseJob = viewModelScope.launch {
            workoutCondition.collectLatest { condition ->
                when (condition) {
                    WorkoutCondition.SET, WorkoutCondition.REST -> runExerciseTimer()
                    WorkoutCondition.PAUSE -> waitForResume()
                    WorkoutCondition.END -> return@collectLatest
                    else -> Unit
                }
            }
        }
        currentExercise = CompletedExercise(0, detail.exerciseId, 0, "", LocalDateTime.now(), completedWorkoutId, 0, 0, 0)
        currentExerciseId = completedExerciseRepository.insert(currentExercise)
        currentExercise.id = currentExerciseId
        _currentExerciseName.value = detail.exerciseName

        for (i in 1..detail.setsNumber) {
            if (workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && workoutCondition.value != WorkoutCondition.END) {

                runSetTimer()
                _currentSet.value = Set(0, currentExerciseId, setSeconds, detail.reps, 0.0, 0, i)
                val setId = setsRepository.insert(_currentSet.value)
                _currentSet.value.id = setId
                _setList.add(_currentSet.value)
                runRestTimer(setId, detail.restDuration)
                _currentSet.value.restDuration = restSeconds
                restSeconds = 0
                currentExercise.setsNumber++
                currentExercise.totalReps += detail.reps
            }
        }
        _setList.clear()
        currentExercise.duration = exerciseSeconds
        completedExerciseRepository.update(currentExercise)
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

    private suspend fun runRestAfterExerciseTimer() {
        while (true) {
            when (workoutCondition.value) {
                WorkoutCondition.REST_AFTER_EXERCISE -> restAfterExerciseStopwatch()
                WorkoutCondition.PAUSE -> waitForResume()
                else -> break
            }
        }
    }

    private suspend fun restAfterExerciseStopwatch() {
        while (workoutCondition.value == WorkoutCondition.REST_AFTER_EXERCISE) {
            exerciseRestSeconds++
            _stringRestTime.value = formatTime(exerciseRestSeconds)
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