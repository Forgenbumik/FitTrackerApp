package com.example.fittrackerapp.uielements.completedworkout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity

class CompletedWorkoutActivity: ComponentActivity()  {
    private lateinit var viewModel: CompletedWorkoutViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding),
                               onExerciseClick = { exercise, exerciseName -> onExerciseClick(exercise, exerciseName) },
                               formatTime = ::formatTime)
                }
            }
        }

        val app = application as App

        val completedWorkoutId = intent.getLongExtra("completedWorkoutId", -1)

        val completedWorkoutRepository = CompletedWorkoutRepository(app.appDatabase.completedWorkoutDao())
        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())

        val factory = CompletedWorkoutViewModelFactory(completedWorkoutId, completedWorkoutRepository, completedExerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(CompletedWorkoutViewModel::class.java)
    }

    fun onExerciseClick(completedExercise: CompletedExercise, exerciseName: String) {
        val intent = Intent(this, CompletedExerciseActivity::class.java).apply {
            putExtra("completedExerciseId", completedExercise.id)
            putExtra("exerciseName", exerciseName)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier, formatTime: (Int) -> String, onExerciseClick: (CompletedExercise, String) -> Unit, viewModel: CompletedWorkoutViewModel = viewModel(), ) {

    val completedExercises = viewModel.completedExercises.collectAsState().value

    Column(
        modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        WorkoutInformation()
        ExercisesList(completedExercises, onExerciseClick)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutInformation(viewModel: CompletedWorkoutViewModel = viewModel()) {

    val completedWorkout = viewModel.completedWorkout.collectAsState().value

    val formattedTime = viewModel.formatTime(completedWorkout.duration)
    val totalExercises = viewModel.getWorkoutTotalExercises()
    val totalSets = viewModel.getWorkoutTotalSets()
    val totalReps = viewModel.getWorkoutTotalReps()

    Column {
        Text("Общее время: ${formattedTime}")
        Text("Всего упражнений: ${totalExercises}")
        Text("Всего подходов: ${totalSets}")
        Text("Всего повторений: ${totalReps}")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesList(completedExercises: List<CompletedExercise>, onExerciseClick: (CompletedExercise, String) -> Unit) {
    LazyColumn {
        items(completedExercises) { exercise ->
            ExerciseItem(exercise, onExerciseClick)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun ExerciseItem(completedExercise: CompletedExercise, onExerciseClick: (CompletedExercise, String) -> Unit, viewModel: CompletedWorkoutViewModel = viewModel()) {

    val exerciseName = remember {mutableStateOf("")}
    val exerciseSetsNumber = remember { mutableStateOf(0) }
    val exerciseTotalReps = remember { mutableStateOf(0) }

    LaunchedEffect(completedExercise.exerciseId) {
        exerciseName.value = viewModel.getExerciseName(completedExercise.exerciseId)
        exerciseSetsNumber.value = viewModel.getExerciseSetsNumber(completedExercise.id)
        exerciseTotalReps.value = viewModel.getExerciseTotalReps(completedExercise.id)
    }

    Row(modifier = Modifier.padding(8.dp).clickable { onExerciseClick(completedExercise, exerciseName.value) }) {
        Text(exerciseName.value)
        Text(" Длительность ${viewModel.formatTime(completedExercise.duration)} ")
        Text("${exerciseSetsNumber.value} подходов ")
        Text("${exerciseTotalReps.value} повторений")
    }
}