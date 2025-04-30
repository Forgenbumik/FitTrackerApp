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
import com.example.fittrackerapp.App
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class WorkoutRecordingService: Service() {

    companion object {
        private const val CHANNEL_ID = "workout_recording_channel"
        private const val NOTIFICATION_ID = 1
    }

    // --- Репозитории ---
    private lateinit var completedWorkoutRepository: CompletedWorkoutRepository
    private lateinit var workoutDetailRepository: WorkoutDetailRepository
    private lateinit var setsRepository: SetRepository
    private lateinit var lastWorkoutRepository: LastWorkoutRepository
    private lateinit var completedExerciseRepository: CompletedExerciseRepository

    // --- Сервисные переменные ---
    private var isTimerStarted = false
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // --- Работа с тренировкой ---
    private var workoutId = 0L
    private var workoutDetailId = 0L
    private lateinit var completedWorkout: CompletedWorkout
    private lateinit var currentSet: Set
    private var currentExerciseId = 0L
    private var setList = mutableListOf<Set>()
    private var exerciseJob: Job? = null
    private var isLastWorkoutInserted = false
    private val _stringWorkoutTime = MutableStateFlow("00:00")
    val stringWorkoutTime: StateFlow<String> = _stringWorkoutTime

    // --- Коммуникатор ---
    private val _completedWorkoutId = WorkoutRecordingCommunicator.completedWorkoutId
    private val _currentExercise = WorkoutRecordingCommunicator.currentExercise
    private val _currentExerciseName = WorkoutRecordingCommunicator.currentExerciseName
    private val _workoutCondition = WorkoutRecordingCommunicator.workoutCondition
    private val _workoutSeconds = WorkoutRecordingCommunicator.workoutSeconds
    private val _exerciseSeconds = WorkoutRecordingCommunicator.exerciseSeconds
    private val _setSeconds = WorkoutRecordingCommunicator.setSeconds
    private val _restSeconds = WorkoutRecordingCommunicator.restSeconds
    private val _changingSet = WorkoutRecordingCommunicator.changingSet
    private val _nextExercise = WorkoutRecordingCommunicator.nextExercise

    private val _lastCondition = WorkoutRecordingCommunicator.lastCondition

    override fun onCreate() {
        super.onCreate()
        val app = application as App
        completedWorkoutRepository = CompletedWorkoutRepository(app.appDatabase.completedWorkoutDao())
        workoutDetailRepository = WorkoutDetailRepository(app.appDatabase.workoutDetailDao())
        setsRepository = SetRepository(app.appDatabase.setDao())
        lastWorkoutRepository = LastWorkoutRepository(app.appDatabase.lastWorkoutDao(), app.appDatabase.workoutDao(), app.appDatabase.exerciseDao())
        completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        workoutId = intent?.getLongExtra("workoutId", -1) ?: -1
        workoutDetailId = intent?.getLongExtra("detailId", -1) ?: -1
        if (!isTimerStarted) {
            completedWorkout = CompletedWorkout(0, 0, "", LocalDateTime.now(), workoutId, 0)
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
                    is ServiceCommand.Set -> setCondition(WorkoutCondition.SET)
                    is ServiceCommand.Rest -> setCondition(WorkoutCondition.REST)
                    is ServiceCommand.Pause -> setCondition(WorkoutCondition.PAUSE)
                    is ServiceCommand.RestAfterExercise -> setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                    is ServiceCommand.End -> setCondition(WorkoutCondition.END)
                    is ServiceCommand.UpdateSet -> updateSet(command.set, command.reps, command.weight)
                    is ServiceCommand.SetChangingSet -> setChangingSet(command.set)
                }
            }
        }
    }
    // -------------------------------------
    // Логика тренировки
    // -------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runWorkout() {
        val id = completedWorkoutRepository.insert(completedWorkout)

        _completedWorkoutId.value = id
        completedWorkout.id = id
        val details = workoutDetailRepository.getByWorkoutId(workoutId).toMutableList()
        val firstExercise = details.first { it.exerciseId == workoutDetailId }
        details.remove(firstExercise)
        details.add(0, firstExercise)
        for (i in 0..(details.size-1)) {
            if (i+1 != details.size) {
                _nextExercise.value = details[i+1]
            }
            runExercise(details[i])
            setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
            runRestAfterExerciseTimer()
            _currentExercise.value?.restDuration  = _restSeconds.value
            _currentExercise.value?.let { completedExerciseRepository.update(it) }
            completedWorkout.exercisesNumber++
        }

        completedWorkout.duration = _workoutSeconds.value
        completedWorkoutRepository.update(completedWorkout)
        delay(1000)
        insertLastWorkout()
        setCondition(WorkoutCondition.END)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertLastWorkout() {
        if (isLastWorkoutInserted) return
        serviceScope.launch {
            lastWorkoutRepository.insertLastWorkout(completedWorkout)
            Log.d("LastWorkout", "Last workout inserted")
        }
        isLastWorkoutInserted = true
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
            when (_workoutCondition.value) {
                WorkoutCondition.SET, WorkoutCondition.REST -> runExerciseTimer()
                WorkoutCondition.PAUSE -> waitForResume()
                WorkoutCondition.END -> return@launch
                else -> Unit
            }
        }

        _currentExercise.value = CompletedExercise(0, detail.exerciseId, 0, "", LocalDateTime.now(), _completedWorkoutId.value, 0, 0, 0)
        _currentExerciseName.value = detail.exerciseName

        for (i in 1..detail.setsNumber) {
            if (_workoutCondition.value != WorkoutCondition.REST_AFTER_EXERCISE
                && _workoutCondition.value != WorkoutCondition.END) {
                runSetTimer()
                currentSet = Set(0, currentExerciseId, _setSeconds.value, detail.reps, 0.0, 0, i)
                val setId = setsRepository.insert(currentSet)
                currentSet.id = setId
                runRestTimer(setId, detail.restDuration)
                _restSeconds.value = 0
                _currentExercise.value!!.setsNumber++
                _currentExercise.value!!.totalReps += detail.reps
            }
        }
        setList.clear()
        _currentExercise.value!!.duration = _exerciseSeconds.value
        _exerciseSeconds.value = 0
        completedExerciseRepository.update(_currentExercise.value!!)
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
            _restSeconds.value++
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