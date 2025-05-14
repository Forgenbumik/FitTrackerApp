package com.example.fittrackerapp.uielements.creatingworkout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.fittrackerapp.uielements.FileIcon
import com.example.fittrackerapp.uielements.addingtousedworkouts.AddingToUsedWorkoutsActivity
import com.example.fittrackerapp.uielements.allworkouts.AllExercisesActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsActivity
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

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
                    MainScreen(Modifier.padding(innerPadding), ::onAddExerciseClick, ::onSaveClick, ::onBackPressed)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, AddingToUsedWorkoutsActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier,
    onAddExerciseClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: CreatingWorkoutViewModel = viewModel()
) {
    val exercises = viewModel.exercisesList
    val workout = viewModel.workout.collectAsState()
    val isShowChangeWindow = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Кнопка "Назад"
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }

            // Заголовок
            Text(
                text = "Создание сценария тренировки",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B9AAA),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Поле для ввода названия тренировки
            NameField(workout.value.name)

            // Список упражнений
            ExercisesList(
                exercises = exercises,
                isShowChangeWindow = isShowChangeWindow,
                onAddExerciseClick = onAddExerciseClick
            )

            // Кнопка "Сохранить", которая остается внизу
            Spacer(modifier = Modifier.weight(1f)) // Этот Spacer заполняет оставшееся пространство

            Button(
                onClick = {
                    viewModel.saveWorkout()
                    onSaveClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
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
        onValueChange = { viewModel.setWorkoutName(it) },
        label = { Text("Название тренировки") },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E1E2E),
            unfocusedContainerColor = Color(0xFF2A2A3A),
            focusedLabelColor = Color(0xFF1B9AAA),
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF1B9AAA)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesList(
    exercises: List<WorkoutDetail>,
    isShowChangeWindow: MutableState<Boolean>,
    onAddExerciseClick: () -> Unit,
    viewModel: CreatingWorkoutViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .padding(8.dp)
    ) {
        LazyColumn {
            items(exercises) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setSelectedExercise(it)
                            isShowChangeWindow.value = true
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            it.exerciseName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${it.setsNumber} подходов, ${it.reps} повт.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        TextButton(onClick = onAddExerciseClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("+ Добавить упражнение", color = Color(0xFF1B9AAA))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChangeWindow(
    isShowChangeWindow: MutableState<Boolean>,
    viewModel: CreatingWorkoutViewModel = viewModel()
) {
    val detail = viewModel.selectedExercise.collectAsState().value

    var selectedSetsNumber by remember { mutableStateOf(detail.setsNumber) }
    var selectedRepsNumber by remember { mutableStateOf(detail.reps) }
    var selectedRestDuration by remember { mutableStateOf(detail.restDuration) }

    ModalBottomSheet(
        onDismissRequest = { isShowChangeWindow.value = false },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Редактировать упражнение",
                color = Color(0xFF1B9AAA),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ListSets(
                    modifier = Modifier.weight(1f),
                    setsNum = selectedSetsNumber,
                    onItemSelected = { selectedSetsNumber = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                ListNumberReps(
                    modifier = Modifier.weight(1f),
                    reps = selectedRepsNumber,
                    onItemSelected = { selectedRepsNumber = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TimePicker(
                selectedRestDuration = selectedRestDuration,
                onTimeSelected = { minutes, seconds ->
                    selectedRestDuration = minutes * 60 + seconds
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { isShowChangeWindow.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Отмена", color = Color.White)
                }

                Button(
                    onClick = {
                        val updatedDetail = detail.copy(
                            setsNumber = selectedSetsNumber,
                            reps = selectedRepsNumber,
                            restDuration = selectedRestDuration
                        )
                        viewModel.updateExerciseDetail(updatedDetail)
                        isShowChangeWindow.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B9AAA))
                ) {
                    Text("ОК", color = Color.Black)
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
fun TimePicker(selectedRestDuration: Int,
    modifier: Modifier = Modifier,
    onTimeSelected: (minutes: Int, seconds: Int) -> Unit
) {
    var selectedMinute by remember { mutableStateOf(selectedRestDuration / 60) }
    var selectedSecond by remember { mutableStateOf(selectedRestDuration % 60) }

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