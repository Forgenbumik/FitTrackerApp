package com.example.fittrackerapp.uielements.executingexercise

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.CenteredPicker
import com.example.fittrackerapp.uielements.VideoPlayerFromFile
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class ExecutingWorkoutActivity : ComponentActivity() {
    private lateinit var viewModel: ExecutingExerciseViewModel

    private var exerciseName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        exerciseName = intent.getStringExtra("exerciseName") ?: "Упражнение"
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        Modifier.padding(innerPadding),
                        exerciseName,
                        onEndClick = { onEndClick() },
                        setCondition = { condition -> setCondition(condition)},
                        formatTime = { secs -> formatTime(secs)})
                }
            }
        }

        val app = application as App

        val exerciseid = intent.getLongExtra("workoutId", -1)
        val plannedSets = intent.getIntExtra("plannedSets", 0)
        val plannedReps = intent.getIntExtra("plannedReps", 0)
        val plannedRestDuration = intent.getIntExtra("plannedRestDuration", 0)

        if (exerciseid == -1L) {
            Log.e("ExecutingExerciseActivity", "Ошибка: exerciseId не передан в Intent!")
        }

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())
        val setsRepository = SetRepository(app.appDatabase.setDao())
        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())
        val lastWorkoutRepository = LastWorkoutRepository(app.appDatabase.lastWorkoutDao(), app.appDatabase.workoutDao(), app.appDatabase.exerciseDao())

        val factory = ExecutingExerciseViewModelFactory(exerciseid, plannedSets, plannedReps, plannedRestDuration, exerciseRepository, completedExerciseRepository, setsRepository, lastWorkoutRepository)

        viewModel = ViewModelProvider(this, factory).get(ExecutingExerciseViewModel::class.java)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {

        viewModel.setCondition(WorkoutCondition.END)

        val intent = Intent(this, MainActivity::class.java)

        lifecycleScope.launch {
            viewModel.isSaveCompleted
                .filter { it } // пропускаем, пока не станет true
                .first()       // ждём первое значение true
            startActivity(intent)
            finish()
        }
        super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEndClick() {

        val intent = Intent(this, CompletedExerciseActivity::class.java).apply {
            putExtra("completedExerciseId", viewModel.completedExerciseId)
        }

        lifecycleScope.launch {
            viewModel.isSaveCompleted
                .filter { it } // пропускаем, пока не станет true
                .first()       // ждём первое значение true
            startActivity(intent)
            finish()
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCondition(condition: WorkoutCondition) {
        viewModel.setCondition(condition)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setChangingSet(set: Set) {
        viewModel.setChangingSet(set)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    exerciseName: String, onEndClick: () -> Unit,
    setCondition: (WorkoutCondition) -> Unit,
    formatTime: (Int) -> String,
    viewModel: ExecutingExerciseViewModel = viewModel()) {

    val exercise = viewModel.exercise

    val stringExerciseTime = viewModel.stringExerciseTime.collectAsState().value

    val stringSetTime = viewModel.stringSetTime.collectAsState()

    val stringRestTime = viewModel.stringRestTime.collectAsState()

    val workoutCondition = viewModel.workoutCondition.collectAsState().value

    if (workoutCondition == WorkoutCondition.END) onEndClick()

    val lastCondition = viewModel.lastCondition.collectAsState().value

    val setList = viewModel.setList

    val context = LocalContext.current

    val isShowChangeWindow = remember { mutableStateOf(false) }

    val changingSet = viewModel.changingSet.collectAsState()
    Column(modifier = modifier) {
        Text(exerciseName)
        if (!(lastCondition == WorkoutCondition.REST_AFTER_EXERCISE && workoutCondition == WorkoutCondition.PAUSE || workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE)) {
            val file = exercise.videoPath?.let { File(context.filesDir, it) }
            if (file != null) {
                VideoPlayerFromFile(file)
            }

            SetsTable(setList, formatTime, isShowChangeWindow)
        }

        Text(stringExerciseTime)
        LastSet(lastCondition, workoutCondition, stringSetTime,
            stringRestTime, setCondition, onEndClick, isShowChangeWindow)
    }

    if (isShowChangeWindow.value && changingSet.value != null) {
        SetChangeWindow(changingSet.value!!, isShowChangeWindow)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, isShowChangeWindow: MutableState<Boolean>) {
    SetsTableHeaders()
    SetsStrings(setList, formatTime, isShowChangeWindow)
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
fun SetsStrings(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, isShowChangeWindow: MutableState<Boolean>, viewModel: ExecutingExerciseViewModel = viewModel()) {

    val setListValue = setList

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(setListValue.size) { i ->  // Используем items вместо for
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Выравнивание по центру
            ) {
                val modifier = Modifier.weight(1f)
                Text("${i + 1}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setListValue[i].reps}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setListValue[i].weight} кг", modifier = modifier, textAlign = TextAlign.Center)
                val setStringSeconds = formatTime(setListValue[i].duration)
                Text(setStringSeconds, modifier = modifier, textAlign = TextAlign.Center)

                Box(modifier = Modifier
                    .aspectRatio(2f)
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Button(onClick = {
                        viewModel.setChangingSet(setListValue[i])
                        isShowChangeWindow.value = true
                    }) {
                        Text("Изменить")
                    }
                }
            }
            HorizontalDivider() // Разделитель между строками
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetChangeWindow(set: Set, isShowChangeWindow: MutableState<Boolean>, viewModel: ExecutingExerciseViewModel = viewModel()) {

    ModalBottomSheet(
        onDismissRequest = { isShowChangeWindow.value = false }
    ) {
        Column {
            Row {
                Text("Повторения")
                Text("Вес")
            }

            var selectedRepsNumber = set.reps

            var selectedWeight = set.weight
            Column {
                Row {
                    val modifier = Modifier.weight(0.5f)
                    ListNumberReps(modifier, set.reps, onItemSelected = { selectedItem ->
                        selectedRepsNumber = selectedItem
                    })
                    ListWeight(modifier, selectedWeight.toInt(), (selectedWeight - selectedWeight.toInt()).toInt(),
                        onItemSelected = { selectedInteger, selectedDecimal -> selectedWeight = selectedInteger + selectedDecimal.toDouble() / 10 })
                }

                Button(
                    onClick = {
                        viewModel.updateSet(set, selectedRepsNumber, selectedWeight)
                        isShowChangeWindow.value = false
                        viewModel.setChangingSet(null)
                    }
                ) {
                    Text("ОК")
                }
            }
        }
    }
}

@Composable
fun ListNumberReps(modifier: Modifier, reps: Int, onItemSelected: (Int) -> Unit) {

    val numberList = (0..100).toList()

    CenteredPicker(items = numberList, selectedIndex = reps, onItemSelected = onItemSelected, modifier = modifier)
}

@Composable
fun ListWeight(modifier: Modifier, integerPart: Int, decimalPart: Int, onItemSelected: (Int, Int) -> Unit) {
    val weightList = (0..500).toList()
    val weightDecimals = (0..9).toList()

    var integerWeight = integerPart

    var decimalWeight = decimalPart

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        CenteredPicker(weightList, selectedIndex = integerPart, onItemSelected = { selectedInteger ->
            integerWeight = selectedInteger
            onItemSelected(integerWeight, decimalWeight)
        }, modifier = modifier)
        Text(".")
        CenteredPicker(weightDecimals, selectedIndex = decimalPart, onItemSelected = { selectedDecimal ->
            decimalWeight = selectedDecimal
            onItemSelected(integerWeight, decimalWeight)
        }, modifier = modifier)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseInformation(nextExercise: State<WorkoutDetail>, stringRestTime: State<String>, formatTime: (Int) -> String) {

    val nextExerciseValue = nextExercise.value

    val stringRestTimeValue = stringRestTime.value

    Column {
        Text("Отдых: ${stringRestTimeValue}$")

        Text(nextExerciseValue.exerciseName)
        if (nextExerciseValue.isRestManually) {
            Text("${nextExerciseValue.setsNumber} подходов, ${ nextExerciseValue.reps } повт., отдых: вручную")
        }
        else {
            Text("${nextExerciseValue.setsNumber} подходов, " +
                    "${ nextExerciseValue.reps } повт., " +
                    "отдых: ${formatTime(nextExerciseValue.restDuration)}")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastSet(lastCondition: WorkoutCondition, workoutCondition: WorkoutCondition,
            stringSetTime: State<String>, stringRestTime: State<String>,
            setCondition: (WorkoutCondition) -> Unit, onEndClick: () -> Unit,
            isShowChangeWindow: MutableState<Boolean>
) {

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
                FirstSetButtons(modifier = modifier, stringSetTime, setCondition, isShowChangeWindow)
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
fun FirstSetButtons(modifier: Modifier, stringSetTime: State<String>, setCondition: (WorkoutCondition) -> Unit,
                    isShowChangeWindow: MutableState<Boolean>
) {

    val stringSetTimeValue = stringSetTime.value

    Row(modifier = modifier) {
        Button(onClick = {
            setCondition(WorkoutCondition.REST)
            isShowChangeWindow.value = true
        }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text("Конец подхода")
        }
        Button(onClick = { }, modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            Text(stringSetTimeValue)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestButtons(stringRestTime: State<String>, setCondition: (WorkoutCondition) -> Unit, modifier: Modifier) {

    val stringRestTimeValue = stringRestTime.value

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
            Text("Отдых: $stringRestTimeValue")
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
            onClick = {
                setCondition(WorkoutCondition.END)
                onEndClick()
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) { Text("Завершить") }
    }
}