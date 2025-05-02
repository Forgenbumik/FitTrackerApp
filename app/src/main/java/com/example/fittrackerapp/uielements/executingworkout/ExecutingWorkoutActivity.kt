package com.example.fittrackerapp.uielements.executingworkout

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
import androidx.compose.runtime.State
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExecutingWorkoutActivity : ComponentActivity() {
    private lateinit var viewModel: ExecutingWorkoutViewModel

    private var workoutName = ""

    private var isSaving = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        workoutName = intent.getStringExtra("workoutName") ?: "Тренировка"
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(
                        modifier = Modifier.padding(innerPadding),
                        workoutName = workoutName,
                        onEndClick = { onEndClick() },
                        setCondition = {condition -> setCondition(condition)},
                        formatTime = {secs -> formatTime(secs)},
                        setChangingSet = {set -> setChangingSet(set)})
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

        val intent = Intent(this, CompletedWorkoutActivity::class.java).apply {
            putExtra("completedWorkoutId", viewModel.completedWorkoutId)
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
fun CompletedExerciseMainScreen(modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                                viewModel: ExecutingWorkoutViewModel = viewModel(),
                                workoutName: String, onEndClick: () -> Unit,
                                setCondition: (WorkoutCondition) -> Unit,
                                formatTime: (Int) -> String,
                                setChangingSet: (Set) -> Unit) {

    val stringWorkoutTime = viewModel.stringWorkoutTime.collectAsState().value

    val exerciseName = viewModel.currentExerciseName.collectAsState().value

    val stringExerciseTime = viewModel.stringExerciseTime.collectAsState().value

    val stringSetTime = viewModel.stringSetTime.collectAsState()

    val stringRestTime = viewModel.stringRestTime.collectAsState()

    val workoutCondition = viewModel.workoutCondition.collectAsState().value

    if (workoutCondition == WorkoutCondition.END) onEndClick()

    val lastCondition = viewModel.lastCondition.collectAsState().value

    val nextExercise = viewModel.nextExercise.collectAsState()

    val setList = viewModel.setList

    val changingSet = viewModel.changingSet.collectAsState()
    Column(modifier = modifier) {
        Text(workoutName)
        Text(stringWorkoutTime)
        Text(exerciseName)
        if (!(lastCondition == WorkoutCondition.REST_AFTER_EXERCISE && workoutCondition == WorkoutCondition.PAUSE || workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE)) {
            SetsTable(setList, formatTime, setChangingSet, changingSet)
        }

        Text(stringExerciseTime)
        if (workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE
                    || lastCondition == WorkoutCondition.REST_AFTER_EXERCISE
                    && workoutCondition == WorkoutCondition.PAUSE) {
            ExerciseInformation(nextExercise, stringRestTime, formatTime)
        }
        LastSet(lastCondition, workoutCondition, stringSetTime,
            stringRestTime, setCondition, onEndClick)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, setChangingSet: (Set) -> Unit, changingSet: State<Set?>) {
    SetsTableHeaders()
    SetsStrings(setList, formatTime, setChangingSet = setChangingSet, changingSet = changingSet)
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
fun SetsStrings(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, viewModel: ExecutingWorkoutViewModel = viewModel(), setChangingSet: (Set) -> Unit, changingSet: State<Set?>) {

    var showSheet = viewModel.isChangingSet.collectAsState().value
    val changingSetValue = changingSet.value

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
                        viewModel.setIsChangingSet(true)

                    }) {
                        Text("Изменить")
                    }
                }
            }
            HorizontalDivider() // Разделитель между строками
        }
    }
    if (showSheet && changingSetValue != null) {
        SetChangeWindow(changingSetValue, setIsChangingSet = { showSheet-> viewModel.setIsChangingSet(showSheet)})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetChangeWindow(set: Set, viewModel: ExecutingWorkoutViewModel = viewModel(), setIsChangingSet: (Boolean) -> Unit, ) {

    ModalBottomSheet(
        onDismissRequest = { setIsChangingSet(false) }
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
                        setIsChangingSet(false)
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

@Composable
fun CenteredPicker(
    items: List<Int>,
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 50.dp
    val visibleItemsCount = 5
    val centerIndex = visibleItemsCount/2

    Box(modifier = modifier.height(itemHeight * visibleItemsCount)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = itemHeight * centerIndex),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            coroutineScope.launch {
                listState.animateScrollToItem(selectedIndex)
            }

            items(items) { item ->
                val center = listState.firstVisibleItemIndex
                val isSelected = item == center

                CenteredListItem(onItemSelected, listState, coroutineScope, item, center, isSelected, itemHeight)
            }
        }

        // Overlay для выделения центра
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color.LightGray.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun CenteredListItem(onItemSelected: (Int) -> Unit, listState: LazyListState, coroutineScope: CoroutineScope, item: Int, centerIndex: Int, isSelected: Boolean, itemHeight: Dp, ) {
    Text(
        text = "$item",
        fontSize = if (isSelected) 24.sp else 16.sp,
        color = if (isSelected) Color.Black else Color.Gray,
        modifier = Modifier
            .height(itemHeight)
            .fillMaxWidth()
            .wrapContentHeight(),
        textAlign = TextAlign.Center
    )
    if (isSelected) {
        onItemSelected(item)
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
fun LastSet(lastCondition: WorkoutCondition, workoutCondition: WorkoutCondition, stringSetTime: State<String>, stringRestTime: State<String>, setCondition: (WorkoutCondition) -> Unit, onEndClick: () -> Unit) {

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
fun FirstSetButtons(modifier: Modifier, stringSetTime: State<String>, setCondition: (WorkoutCondition) -> Unit) {

    val stringSetTimeValue = stringSetTime.value

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