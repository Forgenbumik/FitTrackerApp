package com.example.fittrackerapp.uielements

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.viewmodels.ExecutingWorkoutViewModel
import com.example.fittrackerapp.viewmodels.ExecutingWorkoutViewModelFactory

class ExecutingWorkoutActivity : ComponentActivity() {
    private lateinit var viewModel: ExecutingWorkoutViewModel

    private var workoutName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        workoutName = intent.getStringExtra("workoutName") ?: "Тренировка"
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        workoutName = workoutName,
                        onEndClick = { onEndClick() },
                        setCondition = {condition -> setCondition(condition)},
                        formatTime = {secs -> formatTime(secs)})
                }
            }
        }

        val app = application as App

        val workoutId = intent.getLongExtra("workoutId", -1)
        val detailId = intent.getLongExtra("detailId", -1)

        if (workoutId == -1L) {
            Log.e("WorkoutViewModel", "Ошибка: workoutId не передан в Intent!")
        }

        val setsRepository = SetRepository(app.appDatabase.setDao())
        val workoutDetailRepository = WorkoutDetailRepository(app.appDatabase.workoutDetailDao())
        val completedWorkoutRepository = CompletedWorkoutRepository(app.appDatabase.completedWorkoutDao())
        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())
        val lastWorkoutRepository = LastWorkoutRepository(app.appDatabase.lastWorkoutDao(), app.appDatabase.workoutDao(), app.appDatabase.exerciseDao())

        val factory = ExecutingWorkoutViewModelFactory(workoutId, detailId, workoutDetailRepository,
            setsRepository, completedWorkoutRepository,
            completedExerciseRepository, lastWorkoutRepository)

        viewModel = ViewModelProvider(this, factory).get(ExecutingWorkoutViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEndClick() {
        val intent = Intent(this, ResultsActivity::class.java).apply {
            putExtra("completedWorkoutId", viewModel.completedWorkoutId)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCondition(condition: WorkoutCondition) {
        viewModel.setCondition(condition)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
               viewModel: ExecutingWorkoutViewModel = viewModel(),
               workoutName: String, onEndClick: () -> Unit,
               setCondition: (WorkoutCondition) -> Unit,
               formatTime: (Int) -> String) {

    val stringWorkoutTime = viewModel.stringWorkoutTime.collectAsState().value

    val exerciseName = viewModel.currentExerciseName.collectAsState().value

    val stringExerciseTime = viewModel.stringExerciseTime.collectAsState().value

    val stringSetTime = viewModel.stringSetTime.collectAsState().value

    val stringRestTime = viewModel.stringRestTime.collectAsState().value

    val workoutCondition = viewModel.workoutCondition.collectAsState().value

    val lastCondition = viewModel.lastCondition.collectAsState().value

    val nextExercise = viewModel.nextExercise.collectAsState().value

    val setList = viewModel.setList.collectAsState().value
    Column(modifier = modifier) {
        Text(workoutName)
        Text(stringWorkoutTime)
        Text(exerciseName)
        SetsTable(setList, formatTime)
        Text(stringExerciseTime)
        if (workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE) {
            ExerciseInformation(nextExercise, stringRestTime, formatTime)
        }
        LastSet(lastCondition, workoutCondition, stringSetTime,
            stringRestTime, setCondition, onEndClick)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: List<Set>, formatTime: (Int) -> String) {
    SetsTableHeaders()
    SetsStrings(setList, formatTime)
}

@Composable
fun SetsTableHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)) {

                val modifier = Modifier.weight(1f)
                Text("Подход", modifier = modifier, textAlign = TextAlign.Center)
                Text("Повт.", modifier = modifier, textAlign = TextAlign.Center)
                Text("Вес", modifier = modifier, textAlign = TextAlign.Center)
                Text("Время", modifier = modifier, textAlign = TextAlign.Center)
                Text("Изменить", modifier = modifier, textAlign = TextAlign.Center)
            }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsStrings(setList: List<Set>, formatTime: (Int) -> String) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(setList.size) { i ->  // Используем items вместо for
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Выравнивание по центру
            ) {

                val modifier = Modifier.weight(1f)
                Text("${i + 1}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setList[i].reps}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setList[i].weight} кг", modifier = modifier, textAlign = TextAlign.Center)
                val setStringSeconds = formatTime(setList[i].duration)
                Text(setStringSeconds, modifier = modifier, textAlign = TextAlign.Center)

                Box(modifier = Modifier
                    .aspectRatio(2f)
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Button(onClick = { }) {
                        Text("Изменить")
                    }
                }
            }
            HorizontalDivider() // Разделитель между строками
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseInformation(nextExercise: WorkoutDetail, stringRestTime: String, formatTime: (Int) -> String) {

    Column {
        Text("Отдых: ${stringRestTime}$")

        Text(nextExercise.exerciseName)
        if (nextExercise.isRestManually) {
            Text("${nextExercise.setsNumber} подходов, ${ nextExercise.reps } повт., отдых: вручную")
        }
        else {
            Text("${nextExercise.setsNumber} подходов, " +
                    "${ nextExercise.reps } повт., " +
                    "отдых: ${formatTime(nextExercise.restDuration)}")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastSet(lastCondition: WorkoutCondition, workoutCondition: WorkoutCondition, stringSetTime: String, stringRestTime: String, setCondition: (WorkoutCondition) -> Unit, onEndClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize() // Заполняем весь экран
            .padding(16.dp) // Добавляем отступы, если нужно
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .background(Color.Gray)
                .align(Alignment.BottomCenter) // Размещаем внизу экрана
        ) {
            val modifier = Modifier.weight(1f)
            if (workoutCondition == WorkoutCondition.SET
                || lastCondition == WorkoutCondition.SET
                && workoutCondition == WorkoutCondition.PAUSE) { //если сейчас подход
                FirstSetButtons(modifier = modifier, stringSetTime, setCondition)
            }
            else if (workoutCondition == WorkoutCondition.REST
                || lastCondition == WorkoutCondition.REST
                && workoutCondition == WorkoutCondition.PAUSE) {//если сейчас отдых после подхода
                RestButtons(stringRestTime, setCondition, modifier = modifier)
            }

            if (workoutCondition == WorkoutCondition.SET
                || workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE
                || workoutCondition == WorkoutCondition.REST) { //все кроме паузы (кнопки пауза, далее)
                SecondSetButtons(workoutCondition, lastCondition, setCondition, modifier = modifier)
            } else if (workoutCondition == WorkoutCondition.PAUSE) { //если сейчас пауза (кнопки продолжить, завершить)
                PauseButtons(lastCondition, setCondition, modifier = modifier, onEndClick = onEndClick)
            }
        }
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FirstSetButtons(modifier: Modifier, stringSetTime: String, setCondition: (WorkoutCondition) -> Unit) {
    Row(modifier = modifier) {
        Button(onClick = {
            setCondition(WorkoutCondition.REST)
        }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text("Конец подхода")
        }
        Button(onClick = { }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text(stringSetTime)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestButtons(stringRestTime: String, setCondition: (WorkoutCondition) -> Unit, modifier: Modifier) {
    Row(modifier = modifier) {
        Button(onClick = {
            setCondition(WorkoutCondition.SET)
        }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text("След. подход")
        }
        Button(onClick = { }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text("Отдых: $stringRestTime")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SecondSetButtons(workoutCondition: WorkoutCondition, lastCondition: WorkoutCondition, setCondition: (WorkoutCondition) -> Unit, modifier: Modifier) {

    Row(modifier = modifier) {

        Button(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(), onClick = {
            setCondition(WorkoutCondition.PAUSE)
        }) {
            Text("Пауза")
        }
        Button(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
            onClick = {
                if (workoutCondition == WorkoutCondition.SET
                    || workoutCondition == WorkoutCondition.REST
                    || workoutCondition == WorkoutCondition.PAUSE
                    && (lastCondition == WorkoutCondition.SET || lastCondition == WorkoutCondition.REST)) {
                    setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                }
                else if (workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE) {
                    setCondition(WorkoutCondition.SET)
                }
            }) {
            Text("Далее")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PauseButtons(lastCondition: WorkoutCondition,
                 setCondition: (WorkoutCondition) -> Unit,
                 modifier: Modifier, onEndClick: () -> Unit) {

    Row(modifier = modifier) {

        Button(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(), onClick = {
            setCondition(lastCondition)
        }) {
            Text("Продолжить")
        }
        Button(
            onClick = onEndClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) { Text("Завершить") }
    }
}