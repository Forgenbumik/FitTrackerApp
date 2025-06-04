package com.example.fittrackerapp.uielements.completedexercise

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.ui.theme.Blue
import com.example.fittrackerapp.ui.theme.FirstTeal
import com.example.fittrackerapp.uielements.CenteredPicker
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutViewModel
import com.example.fittrackerapp.uielements.completedworkout.ExercisesList
import com.example.fittrackerapp.uielements.completedworkout.WorkoutInformation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompletedExerciseActivity: ComponentActivity() {

    private val viewModel: CompletedExerciseViewModel by viewModels()

    var exerciseName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding),
                        exerciseName, ::onBackPressed)
                }
            }
        }
        exerciseName = intent.getStringExtra("exerciseName").toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        val intent = Intent(this, CompletedWorkoutActivity::class.java).apply {
            putExtra("completedWorkoutId", viewModel.completedExercise.value.completedWorkoutId)
        }
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier, exerciseName: String, onBackClick: () -> Unit, viewModel: CompletedExerciseViewModel = viewModel()) {

    val completedExercise = viewModel.completedExercise.collectAsState().value
    val setList = viewModel.setList

    val changingSet = viewModel.changingSet.collectAsState()

    val isShowChangeWindow = remember { mutableStateOf(false) }

    val notes = remember { mutableStateOf(completedExercise.notes) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF1B9AAA)
                )
            }

            Spacer(Modifier.height(8.dp))

            ExerciseInformation(setList)

            Spacer(modifier = Modifier.height(16.dp))

            // Прокручиваемая часть
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF015965))
            ) {
                SetsTable(setList, viewModel::formatTime, isShowChangeWindow, viewModel::setChangingSet, viewModel::deleteSet)
            }

            // Нижняя закреплённая панель с заметками
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .padding(16.dp)
            ) {
                NotesField(notes)
            }
        }

    }


    if (isShowChangeWindow.value && changingSet.value != null) {
        SetChangeWindow(changingSet.value!!, isShowChangeWindow)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseInformation(setList: SnapshotStateList<Set>) {

    val exerciseSetsNumber = setList.size

    var exerciseTotalReps = 0

    setList.forEach {
        exerciseTotalReps += it.reps
    }

    Text("Подходов: ${exerciseSetsNumber}. Всего повторений: ${exerciseTotalReps}")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, isShowChangeWindow: MutableState<Boolean>,
              setChangingSet: (Set) -> Unit, deleteSet: (Set) -> Unit) {
    Column {
        SetsTableHeaders()
        SetsStrings(setList, formatTime, isShowChangeWindow, setChangingSet, deleteSet)
    }
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
fun SetsStrings(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, isShowChangeWindow: MutableState<Boolean>,
                setChangingSet: (Set) -> Unit, deleteSet: (Set) -> Unit) {

    val setListValue = setList

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(setListValue.size) { i ->  // Используем items вместо for
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF015965)),
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
                    IconButton(
                        onClick = { deleteSet(setList[i]) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
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
fun SetChangeWindow(set: Set, isShowChangeWindow: MutableState<Boolean>, viewModel: CompletedExerciseViewModel = viewModel()) {

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
                        onItemSelected = { selectedInteger, selectedDecimal ->
                            selectedWeight = selectedInteger + selectedDecimal.toDouble() / 10 })
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
fun NotesField(notes: MutableState<String?>, viewModel: CompletedExerciseViewModel = viewModel()) {

    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Blue,
        unfocusedContainerColor = Blue,
        cursorColor = FirstTeal,
        focusedIndicatorColor = FirstTeal,
        unfocusedIndicatorColor = Color.DarkGray,
        focusedPlaceholderColor = Color.LightGray,
        unfocusedPlaceholderColor = Color.Gray
    )

    TextField(
        value = notes.value.orEmpty(),
        onValueChange = { notes.value = it },
        placeholder = { Text("Название тренировки") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        colors = colors,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done // <- показывает галочку
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.saveWorkoutNotes(notes.value) // <- вызывается при нажатии галочки
            }
        )
    )
}