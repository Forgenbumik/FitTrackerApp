package com.example.fittrackerapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


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
    private var workoutId = 0L
    private var firstDetailId = 0L
    private lateinit var completedWorkout: CompletedWorkout
    private lateinit var currentSet: Set
    private var currentExerciseId = 0L
    private var setList = mutableListOf<Set>()
    private var exerciseJob: Job? = null
    private var isLastWorkoutInserted = false
    private val _stringWorkoutTime = MutableStateFlow("00:00")
    val stringWorkoutTime: StateFlow<String> = _stringWorkoutTime

    // --- Коммуникатор ---
    private val _workoutCondition = WorkoutRecordingCommunicator.workoutCondition
    private val _workoutSeconds = WorkoutRecordingCommunicator.workoutSeconds
    private val _exerciseSeconds = WorkoutRecordingCommunicator.exerciseSeconds
    private val _setSeconds = WorkoutRecordingCommunicator.setSeconds
    private val _restSeconds = WorkoutRecordingCommunicator.restSeconds
    private var _exerciseRestSeconds = WorkoutRecordingCommunicator.exerciseRestSeconds
    private val _changingSet = WorkoutRecordingCommunicator.changingSet
    private val _nextExercise = WorkoutRecordingCommunicator.nextExercise

    private val _lastCondition = WorkoutRecordingCommunicator.lastCondition

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        workoutId = intent?.getLongExtra("workoutId", -1) ?: -1
        firstDetailId = intent?.getLongExtra("detailId", -1) ?: -1
        if (!isTimerStarted) {
            completedWorkout = CompletedWorkout(workoutId =  workoutId)
            serviceScope.launch {
                launch { runWorkoutTimer() }
                launch { runWorkout() }
            }
        }
        isTimerStarted = true
        observeServiceCommands()

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
            WorkoutRecordingCommunicator.serviceCommands.collect { command ->
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

    var completedWorkoutId = 0L

    val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    private var currentExecExercise: CompletedExercise? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runWorkout() {
        completedWorkoutId = completedWorkoutRepository.insert(completedWorkout)
        completedWorkout = completedWorkout.copy(id = completedWorkoutId)
        val details = workoutDetailRepository.getByWorkoutId(workoutId!!).toMutableList()
        val firstExercise = details.first { it.id == firstDetailId }
        details.remove(firstExercise)
        details.add(0, firstExercise)
        for (i in 0..(details.size-1)) {
            if (_workoutCondition.value != WorkoutCondition.END) {
                if (i+1 != details.size) {
                    _nextExercise.value = details[i+1]
                }
                runExercise(details[i])
                currentExecExercise = currentExecExercise?.copy(duration = _exerciseSeconds.value)
                _exerciseSeconds.value = 0
                if (_workoutCondition.value != WorkoutCondition.END) {
                    setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                }
                runRestAfterExerciseTimer()
                if (setList.isNotEmpty()) {
                    completedExerciseRepository.update(currentExecExercise!!)
                }
                else {
                    completedExerciseRepository.delete(currentExecExercise!!)
                    currentExecExercise = null
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
            _stringWorkoutTime.value = formatTime(_workoutSeconds.value)
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
        currentExecExercise = CompletedExercise(exerciseId =  detail.exerciseId, completedWorkoutId = completedWorkoutId)
        val currentExecExerciseId = completedExerciseRepository.insert(currentExecExercise!!)
        currentExecExercise = currentExecExercise?.copy(id = currentExecExerciseId)

        for (i in 1..detail.setsNumber) {
            if (_workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && _workoutCondition.value != WorkoutCondition.END) {

                runSetTimer()
                currentSet = Set(completedExerciseId = currentExecExerciseId,
                    duration =_setSeconds.value, reps = currentSet.reps, setNumber = i)
                val setId = setsRepository.insert(currentSet)
                currentSet = currentSet.copy(id = setId)
                setList.add(currentSet)
                runRestTimer(setId, detail.restDuration)
                currentSet = currentSet.copy(restDuration = _restSeconds.value)
                _restSeconds.value = 0
            }
        }
        currentExecExercise = currentExecExercise?.copy(duration = _exerciseSeconds.value)
        completedExerciseRepository.update(currentExecExercise!!)
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

    private suspend fun runRestTimer(setId: Long, restDuration: Int) {
        while (true) {
            when (_workoutCondition.value) {
                WorkoutCondition.REST -> restStopwatch(restDuration)
                WorkoutCondition.PAUSE -> waitForResume()
                else -> break
            }
        }
        if (setId != 0L) {
            setsRepository.updateRestDuration(setId, _restSeconds.value)
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