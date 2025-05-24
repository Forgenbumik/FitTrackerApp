package com.example.fittrackerapp.uielements.executingworkout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.runtime.State
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.fittrackerapp.WorkoutCondition
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.CenteredPicker
import com.example.fittrackerapp.uielements.VideoPlayerFromFile
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class ExecutingWorkoutActivity : ComponentActivity() {
    private val viewModel: ExecutingWorkoutViewModel by viewModels()

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
                        Modifier.padding(innerPadding),
                        workoutName,
                        onEndClick = { onEndClick() },)
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {

        viewModel.setCondition(WorkoutCondition.END)

        lifecycleScope.launch {
            viewModel.isSaveCompleted
                .filter { it } // пропускаем, пока не станет true
                .first()       // ждём первое значение true
            finish()
            super.onBackPressed()
        }

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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).background(Color(0xFF18181A)),
               workoutName: String, onEndClick: () -> Unit,
               viewModel: ExecutingWorkoutViewModel = viewModel()) {

    val currentExercise = viewModel.currentExercise.collectAsState().value

    val stringExerciseTime = viewModel.stringExerciseTime.collectAsState().value

    val stringSetTime = viewModel.stringSetTime.collectAsState()

    val stringRestTime = viewModel.stringRestTime.collectAsState()

    val workoutCondition = viewModel.workoutCondition.collectAsState().value

    val lastCondition = viewModel.lastCondition.collectAsState().value

    val nextExercise = viewModel.nextExercise.collectAsState()

    val setList = viewModel.setList

    val context = LocalContext.current

    val isShowChangeWindow = remember { mutableStateOf(false) }

    val changingSet = viewModel.changingSet.collectAsState()
    Column(modifier = modifier) {
        Text(workoutName, fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,)
        Spacer(modifier = Modifier.height(12.dp))
        if (currentExercise != null) {
            Text(currentExercise.name, fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold)
        }
        if (!(lastCondition == WorkoutCondition.REST_AFTER_EXERCISE && workoutCondition == WorkoutCondition.PAUSE || workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE)) {
            val file = currentExercise?.videoPath?.let { File(context.filesDir, it) }
            if (file != null) {
                VideoPlayerFromFile(file)
            }

            SetsTable(setList, viewModel::formatTime, isShowChangeWindow, viewModel::setChangingSet)
        }

        Text(stringExerciseTime)
        if (workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE
                    || lastCondition == WorkoutCondition.REST_AFTER_EXERCISE
                    && workoutCondition == WorkoutCondition.PAUSE) {
            ExerciseInformation(nextExercise, stringRestTime, viewModel::formatTime)
        }
        LastSet(lastCondition, workoutCondition, stringSetTime,
            stringRestTime, viewModel::setCondition, onEndClick, isShowChangeWindow)
    }

    if (isShowChangeWindow.value && changingSet.value != null) {
        SetChangeWindow(changingSet.value!!, isShowChangeWindow)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: SnapshotStateList<Set>, formatTime: (Int) -> String,
              isShowChangeWindow: MutableState<Boolean>, setChangingSet: (Set) -> Unit) {
    SetsTableHeaders()
    SetsStrings(setList, formatTime, isShowChangeWindow, setChangingSet)
}

@Composable
fun SetsTableHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF012935))
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
fun SetsStrings(setList: SnapshotStateList<Set>, formatTime: (Int) -> String,
                isShowChangeWindow: MutableState<Boolean>, setChangingSet: (Set) -> Unit) {

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
                    IconButton(onClick = {
                        setChangingSet(setListValue[i])
                        isShowChangeWindow.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать"
                        )
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
fun SetChangeWindow(set: Set, isShowChangeWindow: MutableState<Boolean>, viewModel: ExecutingWorkoutViewModel = viewModel()) {

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
fun ExerciseInformation(
    nextExercise: State<WorkoutDetail?>,
    stringRestTime: State<String>,
    formatTime: (Int) -> String,
    viewModel: ExecutingWorkoutViewModel = viewModel()
) {

    val exerciseName = remember { mutableStateOf("") }

    LaunchedEffect(nextExercise) {
        exerciseName.value = viewModel.getExerciseName(nextExercise.value!!.exerciseId)
    }

    val nextExerciseValue = nextExercise.value
    val stringRestTimeValue = stringRestTime.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F1F))  // Можно добавить фон по желанию
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Отдых: $stringRestTimeValue",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = exerciseName.value,
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (nextExerciseValue != null) {
                if (nextExerciseValue.isRestManually) {
                    Text(
                        text = "${nextExerciseValue.setsNumber} подходов, ${nextExerciseValue.reps} повт., отдых: вручную",
                        fontSize = 18.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "${nextExerciseValue.setsNumber} подходов, ${nextExerciseValue.reps} повт., отдых: ${formatTime(nextExerciseValue.restDuration)}",
                        fontSize = 18.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastSet(lastCondition: WorkoutCondition, workoutCondition: WorkoutCondition,
            stringSetTime: State<String>, stringRestTime: State<String>,
            setCondition: (WorkoutCondition) -> Unit, onEndClick: () -> Unit,
            isShowChangeWindow: MutableState<Boolean>) {

    Box(
        modifier = Modifier
            .fillMaxSize() // Заполняем весь экран
            .padding(16.dp)
            .background(Color(0xFF18181A))// Добавляем отступы, если нужно
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(20.dp)) // Тёмный фон и скругление
                .align(Alignment.BottomCenter)
                .padding(12.dp) // Размещаем внизу экрана
        ) {
            val buttonModifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 4.dp)

        if (workoutCondition == WorkoutCondition.SET
            || (lastCondition == WorkoutCondition.SET && workoutCondition == WorkoutCondition.PAUSE)) {
            FirstSetButtons(modifier = buttonModifier, stringSetTime, setCondition, isShowChangeWindow)
        } else if (workoutCondition == WorkoutCondition.REST
            || (lastCondition == WorkoutCondition.REST && workoutCondition == WorkoutCondition.PAUSE)) {
            RestButtons(stringRestTime, setCondition, modifier = buttonModifier)
        }

        Spacer(modifier = Modifier.height(8.dp)) // Отступ между рядами

        if (workoutCondition == WorkoutCondition.SET
            || workoutCondition == WorkoutCondition.REST_AFTER_EXERCISE
            || workoutCondition == WorkoutCondition.REST) {
            SecondSetButtons(workoutCondition, lastCondition, setCondition, modifier = buttonModifier)
        } else if (workoutCondition == WorkoutCondition.PAUSE) {
            PauseButtons(lastCondition, setCondition, modifier = buttonModifier, onEndClick = onEndClick)
        }
    }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FirstSetButtons(
    modifier: Modifier,
    stringSetTime: State<String>,
    setCondition: (WorkoutCondition) -> Unit,
    isShowChangeWindow: MutableState<Boolean>
) {
    val stringSetTimeValue = stringSetTime.value

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Конец подхода",
            onClick = {
                setCondition(WorkoutCondition.REST)
                isShowChangeWindow.value = true
            },
            modifier = Modifier.weight(1f)
        )
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = stringSetTimeValue,
            onClick = { },
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestButtons(
    stringRestTime: State<String>,
    setCondition: (WorkoutCondition) -> Unit,
    modifier: Modifier
) {
    val stringRestTimeValue = stringRestTime.value

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "След. подход",
            onClick = { setCondition(WorkoutCondition.SET) },
            modifier = Modifier.weight(1f)
        )
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Отдых: $stringRestTimeValue",
            onClick = { },
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SecondSetButtons(
    workoutCondition: WorkoutCondition,
    lastCondition: WorkoutCondition,
    setCondition: (WorkoutCondition) -> Unit,
    modifier: Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Пауза",
            onClick = { setCondition(WorkoutCondition.PAUSE) },
            modifier = Modifier.weight(1f)
        )
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Далее",
            onClick = {
                if (workoutCondition != WorkoutCondition.REST_AFTER_EXERCISE) {
                    setCondition(WorkoutCondition.REST_AFTER_EXERCISE)
                } else {
                    setCondition(WorkoutCondition.SET)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PauseButtons(
    lastCondition: WorkoutCondition,
    setCondition: (WorkoutCondition) -> Unit,
    modifier: Modifier,
    onEndClick: () -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Продолжить",
            onClick = { setCondition(lastCondition) },
            modifier = Modifier.weight(1f)
        )
        com.example.fittrackerapp.uielements.executingexercise.LargeButton(
            text = "Завершить",
            onClick = {
                setCondition(WorkoutCondition.END)
                onEndClick()
            },
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.error,
            textColor = MaterialTheme.colorScheme.onError
        )
    }
}


@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF2C2C2E),
    textColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor
        ),
        elevation = null, // Убираем тень
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}