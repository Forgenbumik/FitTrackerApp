package com.example.fittrackerapp.uielements.executingworkout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.set.SetRepository
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailRepository
import com.example.fittrackerapp.service.ServiceCommand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutRecordingService: Service() {

    companion object {
        private const val CHANNEL_ID = "workout_recording_channel"
        private const val NOTIFICATION_ID = 1
    }

    // --- Репозитории ---
    @Inject lateinit var completedWorkoutRepository: CompletedWorkoutRepository
    @Inject lateinit var workoutDetailRepository: WorkoutDetailRepository
    @Inject lateinit var setsRepository: SetRepository
    @Inject lateinit var lastWorkoutRepository: LastWorkoutRepository
    @Inject lateinit var completedExerciseRepository: CompletedExerciseRepository

    // --- Сервисные переменные ---
    private var isTimerStarted = false
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // --- Работа с тренировкой ---
    private var workoutId = ""
    private var firstDetailId = ""
    private lateinit var completedWorkout: CompletedWorkout
    private var currentSet = Set()
    private var setList = mutableListOf<Set>()
    private var exerciseJob: Job? = null

    // --- Коммуникатор ---
    private val _serviceCommands = WorkoutRecordingCommunicator.serviceCommands
    private val _workoutCondition = WorkoutRecordingCommunicator.workoutCondition
    private val _workoutSeconds = WorkoutRecordingCommunicator.workoutSeconds
    private val _exerciseSeconds = WorkoutRecordingCommunicator.exerciseSeconds
    private val _setSeconds = WorkoutRecordingCommunicator.setSeconds
    private val _restSeconds = WorkoutRecordingCommunicator.restSeconds
    private var _exerciseRestSeconds = WorkoutRecordingCommunicator.exerciseRestSeconds
    private val _changingSet = WorkoutRecordingCommunicator.changingSet
    private val _nextExercise = WorkoutRecordingCommunicator.nextExercise
    private val _isSaveCompleted = WorkoutRecordingCommunicator.isSaveCompleted
    private val _currentExecExercise = WorkoutRecordingCommunicator.currentExecExercise

    private val _lastCondition = WorkoutRecordingCommunicator.lastCondition

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        workoutId = intent?.getStringExtra("workoutId") ?: ""
        firstDetailId = intent?.getStringExtra("detailId") ?: ""
        if (!isTimerStarted) {
            completedWorkout = CompletedWorkout(workoutId = workoutId)
            observeServiceCommands()
            serviceScope.launch {
                runWorkoutTimer()
            }
            serviceScope.launch {
                runWorkout()
            }
        }
        isTimerStarted = true
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Тренировка идет...")
            .setContentText("Ваш прогресс сохраняется!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun observeServiceCommands() {
        serviceScope.launch {
            for (command in _serviceCommands) {
                Log.d("Service", "Received command: $command")
                when (command) {
                    is ServiceCommand.SetCommand -> setCondition(WorkoutCondition.SET)
                    is ServiceCommand.RestCommand -> setCondition(WorkoutCondition.REST)
                    is ServiceCommand.PauseCommand -> setCondition(WorkoutCondition.PAUSE)
                    is ServiceCommand.RestAfterExerciseCommand -> setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                    is ServiceCommand.EndCommand -> setCondition(WorkoutCondition.END)
                    is ServiceCommand.UpdateSetCommand -> updateSet(command.set, command.reps, command.weight)
                    is ServiceCommand.SetChangingSetCommand -> setChangingSet(command.set)
                }
            }
        }
    }
    // -------------------------------------
    // Логика тренировки
    // -------------------------------------

    private val completedWorkoutId = WorkoutRecordingCommunicator.completedWorkoutId

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runWorkout() {

        completedWorkoutId.value = UUID.randomUUID().toString()
        completedWorkout = completedWorkout.copy(id = completedWorkoutId.value)
        completedWorkoutRepository.insert(completedWorkout)
        val details = workoutDetailRepository.getByWorkoutId(workoutId).toMutableList()
        val firstExercise = details.first { it.id == firstDetailId }
        details.remove(firstExercise)
        details.add(0, firstExercise)
        for (i in 0..<details.size) {
            if (_workoutCondition.value != WorkoutCondition.END) {
                if (i+1 != details.size) {
                    _nextExercise.value = details[i+1]
                }
                runExercise(details[i])
                _currentExecExercise.value = _currentExecExercise.value?.copy(duration = _exerciseSeconds.value)
                _exerciseSeconds.value = 0
                if (_workoutCondition.value != WorkoutCondition.END) {
                    setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                }
                runRestAfterExerciseTimer()
                if (setList.isNotEmpty()) {
                    completedExerciseRepository.update(_currentExecExercise.value!!)
                }
                else {
                    completedExerciseRepository.delete(_currentExecExercise.value!!)
                    _currentExecExercise.value = null
                }
                setList.clear()
            }
        }

        completedWorkout = completedWorkout.copy(duration = _workoutSeconds.value)
        completedWorkoutRepository.update(completedWorkout)
        delay(1000)
        insertLastWorkout()
        setCondition(WorkoutCondition.END)
    }

    fun insertLastWorkout() {
        if (_isSaveCompleted.value) return
        serviceScope.launch {
            lastWorkoutRepository.insertLastWorkout(completedWorkout)
            Log.d("LastWorkout", "Last workout inserted")
            _workoutSeconds.value = 0
            _exerciseSeconds.value = 0
            _setSeconds.value = 0
            _restSeconds.value = 0
            _exerciseRestSeconds.value = 0
            _isSaveCompleted.value = true
        }
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        val newSet = set.copy(reps = reps, weight = weight)
        setList[set.setNumber - 1] = newSet
        serviceScope.launch {
            setsRepository.update(newSet)
        }
    }

    private suspend fun runWorkoutTimer() {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.END -> break
                else -> workoutStopwatch()
            }
        }
    }

    suspend fun workoutStopwatch() {
        while (_workoutCondition.value != WorkoutCondition.PAUSE
            && _workoutCondition.value != WorkoutCondition.END) {
            _workoutSeconds.value++
            completedWorkout = completedWorkout.copy(duration = _workoutSeconds.value)
            delay(1000)
        }
    }

    fun setCondition(condition: WorkoutCondition) {
        if (_workoutCondition.value != condition) {
            _lastCondition.value = _workoutCondition.value
            _workoutCondition.value = condition
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runExercise(detail: WorkoutDetail) {

        exerciseJob?.cancel()
        exerciseJob = serviceScope.launch {
            _workoutCondition.collectLatest { condition ->
                when (condition) {
                    WorkoutCondition.SET, WorkoutCondition.REST -> runExerciseTimer()
                    WorkoutCondition.PAUSE -> waitForResume()
                    WorkoutCondition.END -> return@collectLatest
                    else -> Unit
                }
            }
        }
        val currentExecExerciseId = UUID.randomUUID().toString()
        _currentExecExercise.value = CompletedExercise(id = currentExecExerciseId, exerciseId =  detail.exerciseId, completedWorkoutId = completedWorkoutId.value)

        completedExerciseRepository.insert(_currentExecExercise.value!!)

        for (i in 1..detail.setsNumber) {
            if (_workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && _workoutCondition.value != WorkoutCondition.END) {

                runSetTimer()
                val setId = UUID.randomUUID().toString()
                currentSet = Set(id = setId, completedExerciseId = currentExecExerciseId,
                    duration =_setSeconds.value, reps = detail.reps, setNumber = i)

                setsRepository.insert(currentSet)
                setList.add(currentSet)
                runRestTimer(detail.restDuration)
                currentSet = setList[i - 1].copy(restDuration = _restSeconds.value)
                setsRepository.update(currentSet)
                _restSeconds.value = 0
            }
        }
        _currentExecExercise.value = _currentExecExercise.value?.copy(duration = _exerciseSeconds.value)
        completedExerciseRepository.update(_currentExecExercise.value!!)
    }

    private suspend fun runExerciseTimer() {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.SET -> exerciseStopwatch()
                WorkoutCondition.REST -> exerciseStopwatch()
                else -> break
            }
        }
    }

    private suspend fun exerciseStopwatch() {
        while (_workoutCondition.value == WorkoutCondition.SET
            || _workoutCondition.value == WorkoutCondition.REST) {

            delay(10) // Мини-пауза, чтобы убедиться, что состояние не изменилось
            if (_workoutCondition.value != WorkoutCondition.SET
                && _workoutCondition.value != WorkoutCondition.REST) break

            _exerciseSeconds.value++
            delay(1000)
        }
    }

    private suspend fun runRestAfterExerciseTimer() {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.REST_AFTER_EXERCISE -> restAfterExerciseStopwatch()
                WorkoutCondition.PAUSE -> waitForResume()
                else -> break
            }
        }
    }

    private suspend fun restAfterExerciseStopwatch() {
        while (_workoutCondition.value == WorkoutCondition.REST_AFTER_EXERCISE) {
            _exerciseRestSeconds.value++
            delay(1000)
        }
    }

    private suspend fun runSetTimer() {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.SET -> setStopwatch()
                else -> break
            }
        }
    }

    private suspend fun waitForResume() {
        while (_workoutCondition.value == WorkoutCondition.PAUSE
            && _workoutCondition.value != WorkoutCondition.END) {
            delay(500) // Ждём, пока не изменится состояние
        }
    }

    private suspend fun setStopwatch() {
        while (_workoutCondition.value == WorkoutCondition.SET) {
            _setSeconds.value++
            delay(1000)
        }
    }

    private suspend fun runRestTimer(restDuration: Int) {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.REST -> restStopwatch(restDuration)
                WorkoutCondition.PAUSE -> waitForResume()
                else -> break
            }
        }
        if (currentSet.id != "") {
            currentSet = currentSet.copy(restDuration = _restSeconds.value)
            setsRepository.update(currentSet)
        }
        _setSeconds.value = 0
    }

    suspend fun restStopwatch(duration: Int) {
        if (duration > 0) {
            while (_workoutCondition.value == WorkoutCondition.REST
                && _restSeconds.value < duration) {
                _restSeconds.value++
                delay(1000)
            }
        }
        else {
            while (_workoutCondition.value == WorkoutCondition.REST) {
                _restSeconds.value++
                delay(1000)
            }
        }
        if (_restSeconds.value == duration) {
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