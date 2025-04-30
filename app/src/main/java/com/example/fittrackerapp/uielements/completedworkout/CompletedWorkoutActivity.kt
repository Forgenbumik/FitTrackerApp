package com.example.fittrackerapp.uielements.completedworkout

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity

class CompletedWorkoutActivity: ComponentActivity()  {
    private lateinit var viewModel: CompletedWorkoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(modifier = Modifier.padding(innerPadding), onExerciseClick = { exercise, exerciseName -> onExerciseClick(exercise, exerciseName) })
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
}

@Composable
fun CompletedExerciseMainScreen(modifier: Modifier, viewModel: CompletedWorkoutViewModel = viewModel(), onExerciseClick: (CompletedExercise, String) -> Unit) {

    val completedExercises = viewModel.completedExercises.collectAsState().value
    ExercisesList(completedExercises, onExerciseClick)
}

@Composable
fun ExercisesList(completedExercises: List<CompletedExercise>, onExerciseClick: (CompletedExercise, String) -> Unit) {
    LazyColumn {
        items(completedExercises) { exercise ->
            ExerciseItem(exercise, onExerciseClick)
        }
    }
}

@Composable
fun ExerciseItem(completedExercise: CompletedExercise, onExerciseClick: (CompletedExercise, String) -> Unit, viewModel: CompletedWorkoutViewModel = viewModel()) {

    var exerciseName = ""
    LaunchedEffect(completedExercise.exerciseId) {
        exerciseName = viewModel.getExerciseName(completedExercise.exerciseId)
    }

    Row(modifier = Modifier.padding(8.dp).clickable { onExerciseClick(completedExercise, exerciseName) }) {
        Text(exerciseName)
        Text(" ${completedExercise.duration}")
        Text(" ${completedExercise.setsNumber} подходов")
    }

}

