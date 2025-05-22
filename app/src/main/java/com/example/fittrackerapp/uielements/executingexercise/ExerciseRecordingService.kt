package com.example.fittrackerapp.uielements.executingexercise

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
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.service.ServiceCommand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class ExerciseRecordingService: Service() {

    companion object {
        private const val CHANNEL_ID = "exercise_recording_channel"
        private const val NOTIFICATION_ID = 2
    }

    @Inject lateinit var exerciseRepository: ExerciseRepository
    @Inject lateinit var completedExerciseRepository: CompletedExerciseRepository
    @Inject lateinit var setsRepository: SetRepository
    @Inject lateinit var lastWorkoutRepository: LastWorkoutRepository

    // --- Сервисные переменные ---
    private var isTimerStarted = false
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val _exerciseId = ExerciseRecordingCommunicator.exerciseId
    private var completedExerciseId = 0L
    private var plannedSets = 0
    private var plannedReps = 0
    private var plannedRestDuration = 0
    // --- Работа с тренировкой ---
    private var currentSet = Set()
    private var setList = mutableListOf<Set>()
    private val _stringExerciseTime = MutableStateFlow("00:00")
    val stringExerciseTime: StateFlow<String> = _stringExerciseTime

    // --- Коммуникатор ---
    private val _serviceCommands = ExerciseRecordingCommunicator.serviceCommands
    private val _completedExerciseId = ExerciseRecordingCommunicator.completedExerciseId
    private val _workoutCondition = ExerciseRecordingCommunicator.workoutCondition
    private val _exerciseSeconds = ExerciseRecordingCommunicator.exerciseSeconds
    private val _setSeconds = ExerciseRecordingCommunicator.setSeconds
    private val _restSeconds = ExerciseRecordingCommunicator.restSeconds
    private val _changingSet = ExerciseRecordingCommunicator.changingSet
    private val _isSaveCompleted = ExerciseRecordingCommunicator.isSaveCompleted

    private val _lastCondition = ExerciseRecordingCommunicator.lastCondition

    //работа с тренировкой
    @RequiresApi(Build.VERSION_CODES.O)
    private var exercise = Exercise()

    @RequiresApi(Build.VERSION_CODES.O)
    private var completedExercise = CompletedExercise()

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        _exerciseId.value = intent?.getLongExtra("workoutId", -1) ?: -1
        plannedSets = intent?.getIntExtra("plannedSets", -1) ?: -1
        plannedReps = intent?.getIntExtra("plannedReps", -1) ?: -1
        plannedRestDuration = intent?.getIntExtra("plannedRestDuration", -1) ?: -1
        if (!isTimerStarted) {
            completedExercise = CompletedExercise(exerciseId =  _exerciseId.value)
            observeServiceCommands()
            serviceScope.launch {
                launch {
                    exerciseRepository.getByIdFlow(_exerciseId.value).collect {
                        if (it != null) {
                            exercise = it
                        }
                    }
                }
                launch { runExercise() }
                launch { runExerciseTimer() }
            }
        }
        isTimerStarted = true
        return START_STICKY
    }

    private fun startForegroundService() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Recording",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

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

    fun setCondition(condition: WorkoutCondition) {
        if (_workoutCondition.value != condition) {
            _lastCondition.value = _workoutCondition.value
            _workoutCondition.value = condition
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun runExercise() {
        completedExercise = CompletedExercise(exerciseId = _exerciseId.value)
        completedExerciseId = completedExerciseRepository.insert(completedExercise)
        completedExercise = completedExercise.copy(id = completedExerciseId)

        for (i in 1..plannedSets) {
            if (_workoutCondition.value != WorkoutCondition.END) {

                runSetTimer()
                currentSet = Set(completedExerciseId = _completedExerciseId.value, duration =  _setSeconds.value, reps = plannedReps, setNumber =  i)
                val setId = setsRepository.insert(currentSet)
                currentSet = currentSet.copy(id = setId)
                setList.add(currentSet)
                runRestTimer(setId, plannedRestDuration)
                currentSet = currentSet.copy(restDuration = _restSeconds.value)
                _restSeconds.value = 0
            }
        }
        setList.clear()
        completedExercise = completedExercise.copy(duration = _exerciseSeconds.value)
        completedExerciseRepository.update(completedExercise)
        insertLastWorkout()
        setCondition(WorkoutCondition.END)
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
            _stringExerciseTime.value = formatTime(_exerciseSeconds.value)
            delay(1000)
        }
    }

    fun updateSet(set: Set, reps: Int, weight: Double) {
        val newSet = set.copy(reps = reps, weight = weight)
        setList[set.setNumber - 1] = newSet
        serviceScope.launch {
            setsRepository.update(newSet)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertLastWorkout() {
        if (_isSaveCompleted.value) return
        serviceScope.launch {
            lastWorkoutRepository.insertLastWorkout(completedExercise)
            Log.d("LastWorkout", "Last workout inserted")
            _isSaveCompleted.value = true
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

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}