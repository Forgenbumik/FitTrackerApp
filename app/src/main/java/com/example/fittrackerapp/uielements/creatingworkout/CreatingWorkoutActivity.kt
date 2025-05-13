package com.example.fittrackerapp.uielements.creatingworkout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.uielements.CenteredPicker
import com.example.fittrackerapp.uielements.ClickableRow
import com.example.fittrackerapp.uielements.allworkouts.AllExercisesActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsActivity
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CreatingWorkoutActivity: ComponentActivity() {
    private lateinit var viewModel: CreatingWorkoutViewModel

    var workoutId: Long = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding), ::onAddExerciseClick, ::onSaveClick)
                }
            }
        }

        val app = application as App

        val workoutRepository = WorkoutRepository(app.appDatabase.workoutDao())
        val workoutDetailRepository = WorkoutDetailRepository(app.appDatabase.workoutDetailDao())
        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        workoutId = intent.getLongExtra("workoutId", -1)

        val factory = CreatingWorkoutViewModelFactory(workoutId, workoutRepository, workoutDetailRepository, exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(CreatingWorkoutViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val exerciseId = result.data?.getLongExtra("exerciseId", -1)
            viewModel.addExerciseToList(WorkoutDetail(workoutId= workoutId, exerciseId = exerciseId!!))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAddExerciseClick() {
        val intent = Intent(this, AllExercisesActivity::class.java).apply {
            putExtra("reason", "workoutCreating")
        }
        addExerciseLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSaveClick() {
        val intent = Intent(this, UsedWorkoutsActivity::class.java)

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
fun MainScreen(modifier: Modifier, onAddExerciseClick: () -> Unit, onSaveClick: () -> Unit, viewModel: CreatingWorkoutViewModel = viewModel()) {
    val exercises = viewModel.exercisesList

    val workout = viewModel.workout.collectAsState()

    val isShowChangeWindow = remember { mutableStateOf(false) }

    Column(
        modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text("Создание сценария тренировки")
        NameField(workout.value.name)
        ExercisesList(exercises, isShowChangeWindow, onAddExerciseClick)
        Button(onClick = {
            onSaveClick()
        }) {
            Text("Сохранить")
        }
    }
    if (isShowChangeWindow.value) {
        DetailChangeWindow(isShowChangeWindow)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NameField(name: String, viewModel: CreatingWorkoutViewModel = viewModel()) {
    TextField(
        value = name,
        onValueChange = { name ->
            viewModel.setWorkoutName(name)
        },
        label = { Text("Название тренировки") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesList(exercises: List<WorkoutDetail>, isShowChangeWindow: MutableState<Boolean>, onAddExerciseClick: () -> Unit, viewModel: CreatingWorkoutViewModel = viewModel()) {
    LazyColumn {
        items(exercises) {

            Column(
                Modifier.clickable {
                    viewModel.setSelectedExercise(it)
                    isShowChangeWindow.value = true
                }
            ) {
                Text(it.exerciseName)
                Text("${it.setsNumber} подходов, ${it.reps} повт.")
            }
        }
    }
    ClickableRow("+ Добавить упражнение", onClick = {
        onAddExerciseClick()
    })
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChangeWindow(isShowChangeWindow: MutableState<Boolean>, viewModel: CreatingWorkoutViewModel = viewModel()) {

    val detail = viewModel.selectedExercise.collectAsState().value

    ModalBottomSheet(
        onDismissRequest = { isShowChangeWindow.value = false }
    ) {
        Column {
            Row {
                Text("Подходы")
                Text("Повторения")
            }

            var selectedSetsNumber = detail.setsNumber

            var selectedRepsNumber = detail.reps

            var selectedRestDuration = detail.restDuration

            Column {
                Row {
                    val modifier = Modifier.weight(0.5f)

                    ListSets(modifier, selectedSetsNumber,
                        onItemSelected = { selectedItem -> selectedSetsNumber = selectedItem })
                    ListNumberReps(modifier, detail.reps, onItemSelected = { selectedItem ->
                        selectedRepsNumber = selectedItem
                    })
                }
                TimePicker(onTimeSelected = {minutes, seconds ->
                    selectedRestDuration = minutes*60+seconds
                })
                Button(onClick = {
                    isShowChangeWindow.value = false
                }) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        val selectedDetail = detail.copy(
                            setsNumber = selectedSetsNumber,
                            reps = selectedRepsNumber,
                            restDuration = selectedRestDuration)
                        viewModel.updateExerciseDetail(selectedDetail)
                        isShowChangeWindow.value = false
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

    val numberList = (0..500).toList()

    CenteredPicker(items = numberList, selectedIndex = reps, onItemSelected = onItemSelected, modifier = modifier)
}

@Composable
fun ListSets(modifier: Modifier, setsNum: Int, onItemSelected: (Int) -> Unit) {
    val setsNumberList = (0..100).toList()

    CenteredPicker(setsNumberList, selectedIndex = setsNum, onItemSelected = { selected ->
        onItemSelected(selected)
    }, modifier = modifier)
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    onTimeSelected: (minutes: Int, seconds: Int) -> Unit
) {
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedSecond by remember { mutableStateOf(0) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CenteredPicker(
            items = (0..9).toList(),
            modifier = Modifier.weight(1f),
            selectedIndex = selectedMinute,
            onItemSelected = {
                selectedMinute = it
                onTimeSelected(selectedMinute, selectedSecond)
            }
        )

        Text(
            ":",
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        CenteredPicker(
            items = (0..59).toList(),
            modifier = Modifier.weight(1f),
            selectedIndex = selectedSecond,
            onItemSelected = {
                selectedSecond = it
                onTimeSelected(selectedMinute, selectedSecond)
            }
        )
    }
}